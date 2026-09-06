package com.neststay.controller;

import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.service.HomestayRatingService;
import com.neststay.service.HomestayTagService;
import com.neststay.service.RecommendEvalService;
import com.neststay.service.RecommendEventService;
import com.neststay.service.RecommendService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 个性化 / 热门 / 相似民宿。返回字段与 homestayInfo/search 卡片一致。 */
@RestController
@RequestMapping("/recommend")
public class RecommendController {

  @Autowired private RecommendService recommendService;
  @Autowired private RecommendEventService recommendEventService;
  @Autowired private RecommendEvalService recommendEvalService;
  @Autowired private HomestayTagService homestayTagService;
  @Autowired private HomestayRatingService homestayRatingService;

  @IgnoreAuth
  @GetMapping("/personal")
  public R personalRecommend(
      @RequestParam(required = false) Long userId, @RequestParam(defaultValue = "10") int limit) {
    return R.ok().put("data", cards(recommendService.getRecommendations(userId, limit)));
  }

  @IgnoreAuth
  @GetMapping("/hot")
  public R hotRecommend(@RequestParam(defaultValue = "10") int limit) {
    return R.ok().put("data", cards(recommendService.getHotRecommendations(limit)));
  }

  @IgnoreAuth
  @GetMapping("/similar")
  public R similarRecommend(
      @RequestParam Long homestayId, @RequestParam(defaultValue = "6") int limit) {
    return R.ok().put("data", cards(recommendService.getSimilarHomestays(homestayId, limit)));
  }

  @IgnoreAuth
  @GetMapping("/home")
  public R homeRecommend(@RequestParam(required = false) Long userId) {
    List<PlatformViewEntity> rankedHot = recommendService.getHotRecommendations(20);
    List<PlatformViewEntity> personalized =
        userId == null
            ? take(rankedHot, 6)
            : recommendService.getRecommendations(userId, 6);
    if (personalized.isEmpty()) personalized = take(rankedHot, 6);

    Set<Long> used = new HashSet<>();
    for (PlatformViewEntity item : personalized) {
      if (item.getId() != null) used.add(item.getId());
    }
    List<PlatformViewEntity> hot = new ArrayList<>();
    for (PlatformViewEntity item : rankedHot) {
      if (item.getId() != null && used.contains(item.getId())) continue;
      hot.add(item);
      if (hot.size() >= 6) break;
    }
    return R.ok().put("personalized", cards(personalized)).put("hot", cards(hot));
  }

  @IgnoreAuth
  @PostMapping("/event")
  public R event(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (body == null || body.get("listingId") == null) return R.error(400, "缺少房源");
    Long listingId;
    try {
      listingId = Long.valueOf(body.get("listingId").toString());
    } catch (NumberFormatException e) {
      return R.error(400, "房源ID无效");
    }
    String type = body.get("eventType") == null ? "click" : body.get("eventType").toString();
    String sessionId = body.get("sessionId") == null ? null : body.get("sessionId").toString();
    Integer dwell = null;
    if (body.get("dwellMs") != null) {
      try {
        dwell = Integer.valueOf(body.get("dwellMs").toString());
      } catch (NumberFormatException ignored) {
      }
    }
    recommendEventService.record(AuthSupport.userId(request), sessionId, listingId, type, dwell);
    return R.ok();
  }

  @GetMapping("/eval")
  public R eval(@RequestParam(defaultValue = "7") int days, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    return R.ok().put("data", recommendEvalService.summarize(days));
  }

  private List<Map<String, Object>> cards(List<PlatformViewEntity> listings) {
    List<Map<String, Object>> data = new ArrayList<>();
    if (listings == null) return data;
    for (PlatformViewEntity item : listings) {
      data.add(toCard(item));
    }
    homestayTagService.attachToSearchItems(data);
    homestayRatingService.attachDisplay(data);
    return data;
  }

  private static List<PlatformViewEntity> take(List<PlatformViewEntity> source, int limit) {
    if (source == null || source.isEmpty() || limit <= 0) return new ArrayList<>();
    return new ArrayList<>(source.subList(0, Math.min(limit, source.size())));
  }

  private Map<String, Object> toCard(PlatformViewEntity item) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", item.getId());
    result.put("name", item.getHomestayName());
    result.put("image", item.getHomestayImage());
    result.put("location", item.getHomestayLocation());
    result.put("city", item.getHomestayLocation());
    result.put("category", item.getHomestayCategory());
    result.put("layout", item.getLayout());
    result.put("price", item.getPricePerDay());
    result.put("tags", item.getPropertyFeatures() == null ? "" : item.getPropertyFeatures());
    result.put("intro", item.getPropertyIntro());
    result.put("homestayName", item.getHomestayName());
    result.put("homestayImage", item.getHomestayImage());
    result.put("pricePerDay", item.getPricePerDay());
    result.put("merchantName", item.getMerchantName());
    result.put("merchantAccount", item.getMerchantAccount());
    result.put("favoriteCount", item.getFavoriteCount());
    result.put("clickCount", item.getClickCount());
    result.put("sourceType", "platform_view");
    result.put("sourceLabel", "商家民宿");
    result.put("isSelected", item.getIsSelected() != null && item.getIsSelected() == 1);
    result.put("reviewCount", item.getReviewCount() == null ? 0 : item.getReviewCount());
    result.put("avgScore", item.getAvgScore());
    result.put("scoreStay", item.getAvgStay());
    result.put("scoreService", item.getAvgService());
    result.put("scoreQuality", item.getAvgQuality());
    return result;
  }
}
