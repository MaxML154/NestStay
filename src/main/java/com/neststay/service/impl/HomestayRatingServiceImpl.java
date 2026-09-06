package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.neststay.entity.HomestayDiscussEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.service.HomestayDiscussService;
import com.neststay.service.HomestayIndexService;
import com.neststay.service.HomestayRatingService;
import com.neststay.service.PlatformViewService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomestayRatingServiceImpl implements HomestayRatingService {
  public static final int MIN_PUBLIC_REVIEWS = 3;
  public static final double PRIOR_MEAN = 4.2;
  public static final double PRIOR_STRENGTH = 8;

  @Autowired private HomestayDiscussService discussService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private HomestayIndexService homestayIndexService;

  @Override
  public void recalcListing(Long listingId) {
    if (listingId == null) return;
    List<HomestayDiscussEntity> rows =
        discussService.list(new QueryWrapper<HomestayDiscussEntity>().eq("refid", listingId));
    double stay = 0;
    double service = 0;
    double quality = 0;
    double overall = 0;
    int n = 0;
    for (HomestayDiscussEntity row : rows) {
      Double oneStay = firstScore(row.getScoreStay(), row.getScore());
      Double oneService = firstScore(row.getScoreService(), row.getScore());
      Double oneQuality = firstScore(row.getScoreQuality(), row.getScore());
      if (oneStay == null || oneService == null || oneQuality == null) continue;
      stay += oneStay;
      service += oneService;
      quality += oneQuality;
      overall += (oneStay + oneService + oneQuality) / 3.0;
      n++;
    }
    UpdateWrapper<PlatformViewEntity> patch =
        new UpdateWrapper<PlatformViewEntity>().eq("id", listingId).set("review_count", n);
    if (n == 0) {
      patch.set("avg_score", null).set("avg_stay", null).set("avg_service", null).set("avg_quality", null);
    } else {
      patch
          .set("avg_score", round2(overall / n))
          .set("avg_stay", round2(stay / n))
          .set("avg_service", round2(service / n))
          .set("avg_quality", round2(quality / n));
    }
    platformViewService.update(patch);
    homestayIndexService.recalcListing(listingId);
  }

  @Override
  public void attachDisplay(List<Map<String, Object>> items) {
    if (items == null || items.isEmpty()) return;
    Set<String> merchants = new LinkedHashSet<>();
    for (Map<String, Object> item : items) {
      putListingDisplay(item);
      String account = stringValue(item.get("merchantAccount"));
      if (StringUtils.isNotBlank(account)) merchants.add(account);
    }
    Map<String, MerchantStats> stats = merchantStats(merchants);
    for (Map<String, Object> item : items) {
      MerchantStats merchant = stats.get(stringValue(item.get("merchantAccount")));
      if (merchant == null || merchant.count < MIN_PUBLIC_REVIEWS) {
        item.put("merchantRating", null);
        item.put("merchantReviewCount", merchant == null ? 0 : merchant.count);
        continue;
      }
      item.put("merchantRating", displayScore(merchant.avg, merchant.count));
      item.put("merchantReviewCount", merchant.count);
    }
  }

  private void putListingDisplay(Map<String, Object> item) {
    int count = intValue(item.get("reviewCount"));
    Double raw = doubleValue(item.get("avgScore"));
    item.put("rawScore", raw == null ? null : round1(raw));
    item.put("rating", raw == null ? null : displayScore(raw, count));
    item.put("reviewCount", count);
  }

  public static Double displayScore(double raw, int count) {
    if (count < MIN_PUBLIC_REVIEWS) return null;
    return round1((count * raw + PRIOR_STRENGTH * PRIOR_MEAN) / (count + PRIOR_STRENGTH));
  }

  private Map<String, MerchantStats> merchantStats(Collection<String> accounts) {
    Map<String, MerchantStats> result = new LinkedHashMap<>();
    if (accounts == null || accounts.isEmpty()) return result;
    List<PlatformViewEntity> listings =
        platformViewService.list(
            new QueryWrapper<PlatformViewEntity>().in("merchant_account", accounts));
    for (PlatformViewEntity listing : listings) {
      if (listing.getMerchantAccount() == null) continue;
      int count = listing.getReviewCount() == null ? 0 : listing.getReviewCount();
      if (count <= 0 || listing.getAvgScore() == null) continue;
      MerchantStats stats =
          result.computeIfAbsent(listing.getMerchantAccount(), key -> new MerchantStats());
      stats.count += count;
      stats.weightSum += listing.getAvgScore() * count;
    }
    for (MerchantStats stats : result.values()) {
      if (stats.count > 0) stats.avg = stats.weightSum / stats.count;
    }
    return result;
  }

  private static Double firstScore(Double preferred, Double fallback) {
    if (preferred != null) return preferred;
    return fallback;
  }

  private static Double round1(double value) {
    return Math.round(value * 10.0) / 10.0;
  }

  private static Double round2(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private static int intValue(Object value) {
    if (value == null || StringUtils.isBlank(value.toString())) return 0;
    try {
      return (int) Double.parseDouble(value.toString());
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  private static Double doubleValue(Object value) {
    if (value == null || StringUtils.isBlank(value.toString())) return null;
    try {
      return Double.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private static String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private static class MerchantStats {
    int count;
    double weightSum;
    double avg;
  }
}
