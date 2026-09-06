package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.dao.ConsumerDao;
import com.neststay.dao.FavoriteDao;
import com.neststay.dao.HomestayRentalDao;
import com.neststay.dao.PlatformViewDao;
import com.neststay.dao.RecommendEventDao;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RecommendEventEntity;
import com.neststay.service.impl.HomestayIndexServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 三层漏斗：platform_view 候选 → 指数/城市/价位/新鲜度缩到几十条 → 偏好×标签×指数出首页。 热门不再只按 click_count。
 */
@Service
public class RecommendService {

  private static final Logger log = LoggerFactory.getLogger(RecommendService.class);
  private static final int CANDIDATE_CAP = 200;
  private static final List<String> CITIES =
      Arrays.asList("北京", "上海", "杭州", "成都", "大理", "厦门", "青岛", "丽江", "重庆", "西安");

  @Autowired private PlatformViewDao platformViewDao;
  @Autowired private FavoriteDao favoriteDao;
  @Autowired private HomestayRentalDao homestayRentalDao;
  @Autowired private ConsumerDao consumerDao;
  @Autowired private RecommendEventDao recommendEventDao;

  public List<PlatformViewEntity> getRecommendations(Long userId, int limit) {
    List<PlatformViewEntity> all = listPublished();
    if (all.isEmpty()) return Collections.emptyList();
    UserPref pref = buildPref(userId, all);
    List<PlatformViewEntity> candidates = candidateSet(all, pref, userId);
    Map<Long, Double> scores = rankScores(candidates, pref, userId);
    return sortByScore(candidates, scores, Math.max(limit, 0));
  }

  public List<PlatformViewEntity> getHotRecommendations(int limit) {
    List<PlatformViewEntity> all = listPublished();
    Map<Long, Double> scores = hotScores(all);
    return sortByScore(all, scores, Math.max(limit, 0));
  }

  public List<PlatformViewEntity> getSimilarHomestays(Long listingId, int limit) {
    if (listingId == null) return Collections.emptyList();
    PlatformViewEntity target = platformViewDao.selectById(listingId);
    if (target == null) return Collections.emptyList();
    List<PlatformViewEntity> all = listPublished();
    Map<Long, Double> similarity = new HashMap<>();
    for (PlatformViewEntity item : all) {
      if (listingId.equals(item.getId())) continue;
      double sim = computeListingSimilarity(target, item);
      double index = item.getPlatformIndex() == null ? 0.55 : item.getPlatformIndex() / 100.0;
      double fresh = freshness(item);
      similarity.put(item.getId(), sim * 0.7 + index * 0.2 + fresh * 0.1);
    }
    return sortByScore(all, similarity, Math.max(limit, 0));
  }

  private List<PlatformViewEntity> candidateSet(
      List<PlatformViewEntity> all, UserPref pref, Long userId) {
    LinkedHashSet<Long> ids = new LinkedHashSet<>();
    for (PlatformViewEntity item : all) {
      if (sameCity(pref.city, item) || sameCategory(pref.category, item) || priceClose(pref.price, item)) {
        ids.add(item.getId());
      }
      if (HomestayIndexServiceImpl.suggested(item)
          || (item.getIsSelected() != null && item.getIsSelected() == 1)
          || freshness(item) > 0.7) {
        ids.add(item.getId());
      }
    }
    ids.addAll(computeCFScores(userId, all).keySet());
    if (ids.size() < 30) {
      for (PlatformViewEntity item : getHotRecommendations(60)) {
        ids.add(item.getId());
        if (ids.size() >= CANDIDATE_CAP) break;
      }
    }
    Map<Long, PlatformViewEntity> byId =
        all.stream().collect(Collectors.toMap(PlatformViewEntity::getId, item -> item, (a, b) -> a));
    List<PlatformViewEntity> out = new ArrayList<>();
    for (Long id : ids) {
      PlatformViewEntity row = byId.get(id);
      if (row != null) out.add(row);
      if (out.size() >= CANDIDATE_CAP) break;
    }
    return out.isEmpty() ? all : out;
  }

  private Map<Long, Double> rankScores(List<PlatformViewEntity> pool, UserPref pref, Long userId) {
    Map<Long, Double> content = computeContentScores(userId, pool);
    Map<Long, Double> cf = computeCFScores(userId, pool);
    Map<Long, Double> hot = hotScores(pool);
    Map<Long, Double> scores = new HashMap<>();
    boolean coldUser = pref.weak;
    for (PlatformViewEntity item : pool) {
      Long id = item.getId();
      double match = preferenceMatch(pref, item);
      double index = item.getPlatformIndex() == null ? 0.55 : item.getPlatformIndex() / 100.0;
      double selected =
          item.getIsSelected() != null && item.getIsSelected() == 1 ? 0.12 : 0;
      double coldListing = freshness(item) > 0.75 ? 0.08 : 0;
      double clickBoost = clickBoost(userId, id);
      double score =
          match * 0.34
              + index * 0.22
              + content.getOrDefault(id, 0.0) * 0.14
              + cf.getOrDefault(id, 0.0) * 0.10
              + hot.getOrDefault(id, 0.0) * 0.12
              + freshness(item) * 0.08
              + selected
              + coldListing
              + clickBoost;
      if (coldUser && item.getIsSelected() != null && item.getIsSelected() == 1) {
        score += 0.10;
      }
      scores.put(id, score);
    }
    return scores;
  }

  private double preferenceMatch(UserPref pref, PlatformViewEntity item) {
    if (pref.weak) return 0.35;
    double s = 0;
    if (sameCity(pref.city, item)) s += 0.32;
    if (sameCategory(pref.category, item)) s += 0.22;
    if (priceClose(pref.price, item)) s += 0.22;
    Set<String> tags = tagSet(item.getPropertyFeatures());
    if (!pref.tags.isEmpty() && !tags.isEmpty()) {
      Set<String> common = new HashSet<>(pref.tags);
      common.retainAll(tags);
      s += 0.18 * common.size() / pref.tags.size();
    }
    if (pref.guestCount != null && item.getHomestayCount() != null) {
      int rooms = item.getHomestayCount();
      if (rooms >= pref.guestCount || rooms >= 1) s += 0.06;
    }
    return Math.min(s, 1.0);
  }

  private Map<Long, Double> hotScores(List<PlatformViewEntity> all) {
    Map<Long, Double> scores = new HashMap<>();
    int maxClick = all.stream().mapToInt(h -> nvl(h.getClickCount())).max().orElse(1);
    int maxFavorite = all.stream().mapToInt(h -> nvl(h.getFavoriteCount())).max().orElse(1);
    int maxReview = all.stream().mapToInt(h -> nvl(h.getReviewCount())).max().orElse(1);
    for (PlatformViewEntity h : all) {
      double click = maxClick > 0 ? nvl(h.getClickCount()) / (double) maxClick : 0;
      double fav = maxFavorite > 0 ? nvl(h.getFavoriteCount()) / (double) maxFavorite : 0;
      double review = maxReview > 0 ? nvl(h.getReviewCount()) / (double) maxReview : 0;
      double index = h.getPlatformIndex() == null ? 0.55 : h.getPlatformIndex() / 100.0;
      double selected = h.getIsSelected() != null && h.getIsSelected() == 1 ? 0.08 : 0;
      scores.put(
          h.getId(),
          index * 0.40 + click * 0.22 + fav * 0.12 + review * 0.08 + freshness(h) * 0.18 + selected);
    }
    return scores;
  }

  private UserPref buildPref(Long userId, List<PlatformViewEntity> all) {
    UserPref pref = new UserPref();
    if (userId == null) {
      pref.weak = true;
      return pref;
    }
    Map<Long, PlatformViewEntity> byId =
        all.stream().collect(Collectors.toMap(PlatformViewEntity::getId, item -> item, (a, b) -> a));
    List<PlatformViewEntity> seeds = new ArrayList<>();
    for (FavoriteEntity fav : listingFavorites()) {
      if (userId.equals(fav.getUserId()) && byId.containsKey(fav.getRefid())) {
        seeds.add(byId.get(fav.getRefid()));
      }
    }
    ConsumerEntity consumer = consumerDao.selectById(userId);
    if (consumer != null && StringUtils.isNotBlank(consumer.getAccount())) {
      List<HomestayRentalEntity> orders =
          homestayRentalDao.selectList(
              new QueryWrapper<HomestayRentalEntity>()
                  .eq("account", consumer.getAccount())
                  .eq("source_type", "platform_view")
                  .notIn("order_status", Arrays.asList("已取消", "已拒绝"))
                  .orderByDesc("id")
                  .last("limit 8"));
      if (orders != null) {
        for (HomestayRentalEntity order : orders) {
          PlatformViewEntity listing = byId.get(order.getHomestayId());
          if (listing != null) seeds.add(listing);
          if (order.getGuestCount() != null) pref.guestCount = order.getGuestCount();
        }
      }
    }
    try {
      List<RecommendEventEntity> clicks =
          recommendEventDao.selectList(
              new QueryWrapper<RecommendEventEntity>()
                  .eq("user_id", userId)
                  .eq("event_type", "click")
                  .orderByDesc("id")
                  .last("limit 20"));
      if (clicks != null) {
        for (RecommendEventEntity click : clicks) {
          PlatformViewEntity listing = byId.get(click.getListingId());
          if (listing != null) seeds.add(listing);
        }
      }
    } catch (Exception e) {
      log.debug("recommend_event not ready: {}", e.getMessage());
    }
    if (seeds.isEmpty()) {
      pref.weak = true;
      return pref;
    }
    Map<String, Integer> cities = new HashMap<>();
    Map<String, Integer> cats = new HashMap<>();
    int priceSum = 0;
    int priceN = 0;
    for (PlatformViewEntity seed : seeds) {
      String city = cityOf(seed);
      if (StringUtils.isNotBlank(city)) cities.merge(city, 1, Integer::sum);
      if (StringUtils.isNotBlank(seed.getHomestayCategory())) {
        cats.merge(seed.getHomestayCategory(), 1, Integer::sum);
      }
      if (seed.getPricePerDay() != null) {
        priceSum += seed.getPricePerDay();
        priceN++;
      }
      pref.tags.addAll(tagSet(seed.getPropertyFeatures()));
    }
    pref.city = topKey(cities);
    pref.category = topKey(cats);
    pref.price = priceN == 0 ? null : priceSum / priceN;
    pref.weak = false;
    return pref;
  }

  private double clickBoost(Long userId, Long listingId) {
    if (userId == null || listingId == null) return 0;
    try {
      long n =
          recommendEventDao.selectCount(
              new QueryWrapper<RecommendEventEntity>()
                  .eq("user_id", userId)
                  .eq("listing_id", listingId)
                  .eq("event_type", "click"));
      if (n >= 2) return -0.04;
      if (n == 1) return 0.03;
    } catch (Exception ignored) {
    }
    return 0;
  }

  private List<PlatformViewEntity> listPublished() {
    QueryWrapper<PlatformViewEntity> wrapper = new QueryWrapper<>();
    wrapper
        .isNotNull("merchant_account")
        .ne("merchant_account", "")
        .ne("merchant_account", "platform")
        .and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"));
    List<PlatformViewEntity> rows = platformViewDao.selectList(wrapper);
    return rows == null ? Collections.emptyList() : rows;
  }

  private Map<Long, Double> computeCFScores(Long userId, List<PlatformViewEntity> all) {
    Map<Long, Double> scores = new HashMap<>();
    if (userId == null) return scores;
    try {
      Set<Long> catalog =
          all.stream().map(PlatformViewEntity::getId).collect(Collectors.toSet());
      List<FavoriteEntity> listingFavorites = listingFavorites();
      Set<Long> mine = new HashSet<>();
      Map<Long, Set<Long>> others = new HashMap<>();
      for (FavoriteEntity fav : listingFavorites) {
        if (fav.getUserId() == null || fav.getRefid() == null) continue;
        if (!catalog.contains(fav.getRefid())) continue;
        if (fav.getUserId().equals(userId)) {
          mine.add(fav.getRefid());
        } else {
          others.computeIfAbsent(fav.getUserId(), key -> new HashSet<>()).add(fav.getRefid());
        }
      }
      if (mine.isEmpty()) return scores;
      Map<Long, Double> userSimilarity = new HashMap<>();
      for (Map.Entry<Long, Set<Long>> entry : others.entrySet()) {
        Set<Long> otherFavs = entry.getValue();
        Set<Long> intersection = new HashSet<>(mine);
        intersection.retainAll(otherFavs);
        Set<Long> union = new HashSet<>(mine);
        union.addAll(otherFavs);
        if (union.isEmpty()) continue;
        double similarity = (double) intersection.size() / union.size();
        if (similarity > 0.05) userSimilarity.put(entry.getKey(), similarity);
      }
      for (Map.Entry<Long, Double> simEntry : userSimilarity.entrySet()) {
        for (Long listingId : others.get(simEntry.getKey())) {
          if (!mine.contains(listingId)) {
            scores.merge(listingId, simEntry.getValue(), Double::sum);
          }
        }
      }
    } catch (Exception e) {
      log.warn("CF score computation failed: {}", e.getMessage());
    }
    return scores;
  }

  private Map<Long, Double> computeContentScores(Long userId, List<PlatformViewEntity> all) {
    Map<Long, Double> scores = new HashMap<>();
    if (userId == null) return scores;
    try {
      List<Long> seedIds =
          listingFavorites().stream()
              .filter(fav -> userId.equals(fav.getUserId()) && fav.getRefid() != null)
              .map(FavoriteEntity::getRefid)
              .distinct()
              .limit(5)
              .collect(Collectors.toList());
      if (seedIds.isEmpty()) return scores;
      Map<Long, PlatformViewEntity> byId =
          all.stream().collect(Collectors.toMap(PlatformViewEntity::getId, item -> item, (a, b) -> a));
      List<PlatformViewEntity> seeds = new ArrayList<>();
      for (Long id : seedIds) {
        PlatformViewEntity seed = byId.get(id);
        if (seed != null) seeds.add(seed);
      }
      if (seeds.isEmpty()) return scores;
      Set<Long> seedSet = new HashSet<>(seedIds);
      for (PlatformViewEntity candidate : all) {
        if (seedSet.contains(candidate.getId())) continue;
        double total = 0;
        for (PlatformViewEntity seed : seeds) {
          total += computeListingSimilarity(seed, candidate);
        }
        double avg = total / seeds.size();
        if (avg > 0.1) scores.put(candidate.getId(), avg);
      }
    } catch (Exception e) {
      log.warn("Content score computation failed: {}", e.getMessage());
    }
    return scores;
  }

  private double computeListingSimilarity(PlatformViewEntity a, PlatformViewEntity b) {
    double score = 0;
    String cityA = cityOf(a);
    String cityB = cityOf(b);
    if (StringUtils.isNotBlank(cityA) && cityA.equals(cityB)) score += 0.30;
    if (StringUtils.isNotBlank(a.getHomestayCategory())
        && a.getHomestayCategory().equals(b.getHomestayCategory())) {
      score += 0.20;
    }
    if (a.getPricePerDay() != null && b.getPricePerDay() != null && a.getPricePerDay() > 0) {
      double priceDiff =
          Math.abs(a.getPricePerDay() - b.getPricePerDay()) / (double) a.getPricePerDay();
      if (priceDiff < 0.35) score += 0.20 * (1 - priceDiff);
    }
    if (a.getAvgScore() != null && b.getAvgScore() != null) {
      double diff = Math.abs(a.getAvgScore() - b.getAvgScore());
      if (diff <= 0.6) score += 0.10 * (1 - diff / 0.6);
    }
    Set<String> tagsA = tagSet(a.getPropertyFeatures());
    Set<String> tagsB = tagSet(b.getPropertyFeatures());
    if (!tagsA.isEmpty()) {
      Set<String> common = new HashSet<>(tagsA);
      common.retainAll(tagsB);
      score += 0.20 * ((double) common.size() / tagsA.size());
    }
    return Math.min(score, 1.0);
  }

  private List<FavoriteEntity> listingFavorites() {
    QueryWrapper<FavoriteEntity> wrapper = new QueryWrapper<>();
    wrapper.eq("table_name", "platform_view").and(w -> w.eq("type", "1").or().isNull("type"));
    List<FavoriteEntity> rows = favoriteDao.selectList(wrapper);
    return rows == null ? Collections.emptyList() : rows;
  }

  private static List<PlatformViewEntity> sortByScore(
      List<PlatformViewEntity> items, Map<Long, Double> scores, int limit) {
    return items.stream()
        .sorted(
            Comparator.comparingDouble(
                    (PlatformViewEntity a) -> scores.getOrDefault(a.getId(), 0.0))
                .reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  private static boolean sameCity(String city, PlatformViewEntity item) {
    return StringUtils.isNotBlank(city) && city.equals(cityOf(item));
  }

  private static boolean sameCategory(String category, PlatformViewEntity item) {
    return StringUtils.isNotBlank(category) && category.equals(item.getHomestayCategory());
  }

  private static boolean priceClose(Integer price, PlatformViewEntity item) {
    if (price == null || item.getPricePerDay() == null || price <= 0) return false;
    return Math.abs(item.getPricePerDay() - price) / (double) price < 0.35;
  }

  private static double freshness(PlatformViewEntity item) {
    Date created = item.getCreateTime();
    if (created == null) return 0.4;
    long days = TimeUnit.MILLISECONDS.toDays(Math.max(0, System.currentTimeMillis() - created.getTime()));
    if (days <= 7) return 1.0;
    if (days <= 21) return 0.7;
    if (days <= 60) return 0.4;
    return 0.15;
  }

  private static String cityOf(PlatformViewEntity listing) {
    String location = listing == null ? null : listing.getHomestayLocation();
    if (StringUtils.isBlank(location)) return "";
    for (String city : CITIES) {
      if (location.contains(city)) return city;
    }
    return location.length() >= 2 ? location.substring(0, 2) : location;
  }

  private static Set<String> tagSet(String raw) {
    if (StringUtils.isBlank(raw)) return Collections.emptySet();
    return Arrays.stream(raw.split("[,，]"))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());
  }

  private static String topKey(Map<String, Integer> counts) {
    return counts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("");
  }

  private static int nvl(Integer value) {
    return value == null ? 0 : value;
  }

  private static final class UserPref {
    String city = "";
    String category = "";
    Integer price;
    Integer guestCount;
    Set<String> tags = new HashSet<>();
    boolean weak;
  }
}
