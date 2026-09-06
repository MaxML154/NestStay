package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.HomestayInfoView;
import com.neststay.service.HomestayInfoService;
import com.neststay.service.HomestayRatingService;
import com.neststay.service.HomestayTagService;
import com.neststay.service.MerchantStayLookup;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RecommendService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 民宿信息后端接口 */
@RestController
@RequestMapping("/homestayInfo")
public class HomestayInfoController {
  private static final int MAX_SEARCH_CANDIDATES = 10000;

  @Autowired private HomestayInfoService homestayInfoService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private MerchantStayLookup merchantStayLookup;
  @Autowired private HomestayTagService homestayTagService;
  @Autowired private HomestayRatingService homestayRatingService;
  @Autowired private RecommendService recommendService;

  /** 消费者端民宿搜索：只返回已归属商家的房源。精选是 is_selected 标记。 */
  @IgnoreAuth
  @RequestMapping("/search")
  public R search(@RequestParam Map<String, Object> params) {
    String keyword = firstValue(params, "keyword", "name");
    String location = firstValue(params, "location", "city");
    String category = firstValue(params, "category", "homestayCategory");
    String sourceType = firstValue(params, "sourceType");
    Integer minPrice = integerValue(params, "minPrice", "xianjiastart", "priceStart");
    Integer maxPrice = integerValue(params, "maxPrice", "xianjiaend", "priceEnd");
    Long tagId = longValue(params, "tagId");
    int page = positiveInteger(params.get("page"), 1);
    int limit = Math.min(positiveInteger(params.get("limit"), 10), 100);
    if (StringUtils.isNotBlank(sourceType)
        && !"homestay_info".equals(sourceType)
        && !"platform_view".equals(sourceType)
        && !"selected".equals(sourceType)) {
      return R.error(400, "不支持的民宿来源");
    }
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
      return R.error(400, "最低价格不能高于最高价格");
    }
    long requiredCandidates = (long) page * limit;
    if (requiredCandidates > MAX_SEARCH_CANDIDATES) {
      return R.error(400, "搜索范围过大，请缩小筛选条件或页码");
    }
    int candidateLimit = (int) requiredCandidates;
    String sort = firstValue(params, "sort");
    String order = firstValue(params, "order");
    boolean selectedOnly = "selected".equals(sourceType) || "homestay_info".equals(sourceType);

    List<Map<String, Object>> results = new ArrayList<>();
    QueryWrapper<PlatformViewEntity> platformWrapper = merchantStayWrapper();
    if (selectedOnly) {
      platformWrapper.eq("is_selected", 1);
    }
    if (StringUtils.isNotBlank(keyword)) {
      platformWrapper.and(
          wrapper ->
              wrapper
                  .like("homestay_name", keyword)
                  .or()
                  .like("property_intro", keyword)
                  .or()
                  .like("property_features", keyword)
                  .or()
                  .like("merchant_name", keyword));
    }
    if (StringUtils.isNotBlank(location)) {
      platformWrapper.like("homestay_location", location);
    }
    if (StringUtils.isNotBlank(category) && !"普通民宿".equals(category)) {
      platformWrapper.eq("homestay_category", category);
    }
    if (minPrice != null) platformWrapper.ge("price_per_day", minPrice);
    if (maxPrice != null) platformWrapper.le("price_per_day", maxPrice);
    if (tagId != null) {
      java.util.List<Long> listingIds = homestayTagService.listingIdsByTag(tagId);
      if (listingIds.isEmpty()) {
        return R.ok().put("data", new PageUtils(new ArrayList<>(), 0, limit, page));
      }
      platformWrapper.in("id", listingIds);
    }
    long total = platformViewService.count(platformWrapper);
    applySourceSort(platformWrapper, sort, order, "price_per_day");
    Page<PlatformViewEntity> platformPage =
        platformViewService.page(new Page<>(1, candidateLimit, false), platformWrapper);
    for (PlatformViewEntity item : platformPage.getRecords()) {
      results.add(platformSearchItem(item));
    }
    homestayTagService.attachToSearchItems(results);
    homestayRatingService.attachDisplay(results);

    sortSearchResults(results, sort, order);
    int fromIndex = (int) Math.min((long) (page - 1) * limit, results.size());
    int toIndex = Math.min(fromIndex + limit, results.size());
    return R.ok()
        .put(
            "data",
            new PageUtils(
                new ArrayList<>(results.subList(fromIndex, toIndex)), total, limit, page));
  }

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      HomestayInfoEntity entity,
      @RequestParam(required = false) Double xianjiastart,
      @RequestParam(required = false) Double xianjiaend,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    return R.ok().put("data", query(params, entity, xianjiastart, xianjiaend));
  }

  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      HomestayInfoEntity entity,
      @RequestParam(required = false) Double xianjiastart,
      @RequestParam(required = false) Double xianjiaend,
      HttpServletRequest request) {
    QueryWrapper<HomestayInfoEntity> wrapper = new QueryWrapper<>();
    applySearch(params, wrapper);
    if (xianjiastart != null) wrapper.ge("current_price", xianjiastart);
    if (xianjiaend != null) wrapper.le("current_price", xianjiaend);
    wrapper.and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"));
    return R.ok().put("data", query(params, entity, wrapper));
  }

  private PageUtils query(
      Map<String, Object> params, HomestayInfoEntity entity, Double priceStart, Double priceEnd) {
    QueryWrapper<HomestayInfoEntity> wrapper = new QueryWrapper<>();
    if (priceStart != null) wrapper.ge("current_price", priceStart);
    if (priceEnd != null) wrapper.le("current_price", priceEnd);
    return query(params, entity, wrapper);
  }

  private PageUtils query(
      Map<String, Object> params,
      HomestayInfoEntity entity,
      QueryWrapper<HomestayInfoEntity> wrapper) {
    return homestayInfoService.queryPage(
        params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(wrapper, entity), params), params));
  }

  private void applySearch(Map<String, Object> params, QueryWrapper<HomestayInfoEntity> wrapper) {
    applyLike(params, wrapper, "name", "display_name");
    applyLike(params, wrapper, "city", "city");
    applyLike(params, wrapper, "qu", "district");
  }

  private void applyLike(
      Map<String, Object> params,
      QueryWrapper<HomestayInfoEntity> wrapper,
      String requestField,
      String column) {
    Object value = params.remove(requestField);
    if (value != null && StringUtils.isNotBlank(value.toString())) {
      wrapper.like(column, value.toString().replace("%", ""));
    }
  }

  @RequestMapping("/lists")
  public R lists(HomestayInfoEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<HomestayInfoEntity> wrapper = new QueryWrapper<>();
    wrapper.allEq(MPUtil.allEQMapPre(entity, "homestay_info"));
    return R.ok().put("data", homestayInfoService.selectListView(wrapper));
  }

  @RequestMapping("/query")
  public R queryView(HomestayInfoEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<HomestayInfoEntity> wrapper = new QueryWrapper<>();
    wrapper.allEq(MPUtil.allEQMapPre(entity, "homestay_info"));
    HomestayInfoView view = homestayInfoService.selectView(wrapper);
    return R.ok("查询民宿信息成功").put("data", view);
  }

  @RequestMapping({"/info/{id}", "/detail/{id}"})
  @IgnoreAuth
  public R detail(@PathVariable("id") Long id) {
    HomestayInfoEntity entity = homestayInfoService.getById(id);
    if (entity == null) return R.error(404, "民宿不存在");
    entity.setClickCount(entity.getClickCount() == null ? 1 : entity.getClickCount() + 1);
    entity.setClickTime(new java.util.Date());
    homestayInfoService.updateById(entity);
    HomestayInfoView view =
        homestayInfoService.selectView(new QueryWrapper<HomestayInfoEntity>().eq("id", id));
    return R.ok().put("data", view);
  }

  @RequestMapping("/save")
  public R save(@RequestBody HomestayInfoEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || StringUtils.isBlank(entity.getName())) {
      return R.error(400, "民宿名称不能为空");
    }
    if (StringUtils.isBlank(entity.getAuditStatus())) {
      entity.setAuditStatus("已通过");
    }
    homestayInfoService.save(entity);
    return R.ok();
  }

  @RequestMapping("/add")
  public R add(@RequestBody HomestayInfoEntity entity, HttpServletRequest request) {
    return save(entity, request);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody HomestayInfoEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || entity.getId() == null) {
      return R.error(400, "民宿ID不能为空");
    }
    if (homestayInfoService.getById(entity.getId()) == null) {
      return R.error(404, "民宿不存在");
    }
    homestayInfoService.updateById(entity);
    return R.ok();
  }

  @RequestMapping("/select")
  public R toggleSelected(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    Long id = toLong(body == null ? null : body.get("id"));
    if (id == null) return R.error(400, "民宿ID不能为空");
    HomestayInfoEntity existing = homestayInfoService.getById(id);
    if (existing == null) return R.error(404, "民宿不存在");
    existing.setIsSelected(selectedInt(body.get("selected"), existing.getIsSelected()));
    homestayInfoService.updateById(existing);
    return R.ok().put("data", existing);
  }

  private Long toLong(Object value) {
    if (value == null || StringUtils.isBlank(value.toString())) return null;
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private int selectedInt(Object value, Integer current) {
    if (value == null) return current != null && current == 1 ? 0 : 1;
    String text = value.toString();
    if ("1".equals(text) || "true".equalsIgnoreCase(text)) return 1;
    return 0;
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    homestayInfoService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  @RequestMapping("/audit")
  public R audit(@RequestBody HomestayInfoEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || entity.getId() == null || StringUtils.isBlank(entity.getAuditStatus())) {
      return R.error(400, "缺少审核结果");
    }
    HomestayInfoEntity existing = homestayInfoService.getById(entity.getId());
    if (existing == null) return R.error(404, "民宿不存在");
    existing.setAuditStatus(entity.getAuditStatus());
    homestayInfoService.updateById(existing);
    return R.ok().put("data", existing);
  }

  @IgnoreAuth
  @RequestMapping("/bookable")
  public R bookable(@RequestParam Long id, @RequestParam(defaultValue = "platform_view") String sourceType) {
    PlatformViewEntity stay = merchantStayLookup.find(sourceType, id);
    if (stay == null && !"homestay_info".equals(sourceType)) {
      stay = merchantStayLookup.find("homestay_info", id);
    }
    if (stay == null && !"platform_view".equals(sourceType)) {
      stay = merchantStayLookup.find("platform_view", id);
    }
    if (stay == null) return R.error(404, "民宿不存在或不属于商家");
    Map<String, Object> item = platformSearchItem(stay);
    homestayTagService.attachToSearchItems(java.util.Collections.singletonList(item));
    homestayRatingService.attachDisplay(java.util.Collections.singletonList(item));
    return R.ok().put("data", item);
  }

  @IgnoreAuth
  @RequestMapping("/related")
  public R related(@RequestParam Long id, @RequestParam(defaultValue = "platform_view") String sourceType) {
    List<Map<String, Object>> data = new ArrayList<>();
    if (!"platform_view".equals(sourceType)) return R.ok().put("data", data);
    for (PlatformViewEntity item : recommendService.getSimilarHomestays(id, 4)) {
      data.add(platformSearchItem(item));
    }
    homestayTagService.attachToSearchItems(data);
    homestayRatingService.attachDisplay(data);
    return R.ok().put("data", data);
  }

  @IgnoreAuth
  @RequestMapping("/recommend")
  public R recommend(@RequestParam(required = false) Long id) {
    List<Map<String, Object>> data = new ArrayList<>();
    for (PlatformViewEntity item : recommendService.getHotRecommendations(8)) {
      if (id != null && id.equals(item.getId())) continue;
      data.add(platformSearchItem(item));
      if (data.size() >= 4) break;
    }
    homestayTagService.attachToSearchItems(data);
    homestayRatingService.attachDisplay(data);
    return R.ok().put("data", data);
  }

  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      HomestayInfoEntity entity,
      HttpServletRequest request,
      String pre) {
    return recommend(null);
  }

  private QueryWrapper<PlatformViewEntity> merchantStayWrapper() {
    QueryWrapper<PlatformViewEntity> wrapper = new QueryWrapper<>();
    wrapper.isNotNull("merchant_account").ne("merchant_account", "").ne("merchant_account", "platform");
    wrapper.and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"));
    return wrapper;
  }

  private Map<String, Object> platformSearchItem(PlatformViewEntity item) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", item.getId());
    result.put("name", item.getHomestayName());
    result.put("image", item.getHomestayImage());
    result.put("location", item.getHomestayLocation());
    result.put("city", item.getHomestayLocation());
    result.put("category", item.getHomestayCategory());
    result.put("layout", item.getLayout());
    result.put("price", item.getPricePerDay());
    result.put("tags", "");
    result.put("video", item.getVideoIntro());
    result.put("intro", item.getPropertyIntro());
    result.put("address", item.getHomestayLocation());
    result.put("homestayName", item.getHomestayName());
    result.put("homestayImage", item.getHomestayImage());
    result.put("pricePerDay", item.getPricePerDay());
    result.put("merchantName", item.getMerchantName());
    result.put("merchantAccount", item.getMerchantAccount());
    result.put("merchantPhone", item.getMerchantPhone());
    result.put("favoriteCount", item.getFavoriteCount());
    result.put("clickCount", item.getClickCount());
    result.put("createTime", item.getCreateTime());
    result.put("sourceType", "platform_view");
    result.put("sourceLabel", "商家民宿");
    result.put("detailPath", "/index/homestayInfoDetail");
    result.put("isSelected", selectedFlag(item.getIsSelected()));
    result.put("reviewCount", item.getReviewCount() == null ? 0 : item.getReviewCount());
    result.put("avgScore", item.getAvgScore());
    result.put("scoreStay", item.getAvgStay());
    result.put("scoreService", item.getAvgService());
    result.put("scoreQuality", item.getAvgQuality());
    return result;
  }

  private void sortSearchResults(List<Map<String, Object>> results, String sort, String order) {
    String field = "price".equals(sort) || "createTime".equals(sort) ? sort : "clickCount";
    boolean ascending = "asc".equalsIgnoreCase(order);
    Comparator<Map<String, Object>> comparator;
    if ("createTime".equals(field)) {
      Comparator<java.util.Date> dateComparator =
          ascending ? Comparator.naturalOrder() : Comparator.reverseOrder();
      comparator =
          Comparator.comparing(
              item -> (java.util.Date) item.get("createTime"),
              Comparator.nullsLast(dateComparator));
    } else {
      comparator = Comparator.comparingInt(item -> numberValue(item.get(field)));
      if (!ascending) comparator = comparator.reversed();
    }
    Comparator<Map<String, Object>> selectedFirst =
        "clickCount".equals(field)
            ? Comparator.comparingInt(item -> Boolean.TRUE.equals(item.get("isSelected")) ? 0 : 1)
            : (left, right) -> 0;
    results.sort(
        selectedFirst
            .thenComparing(comparator)
            .thenComparing(item -> String.valueOf(item.get("sourceType")))
            .thenComparingLong(item -> Long.parseLong(String.valueOf(item.get("id")))));
  }

  private boolean selectedFlag(Integer value) {
    return value != null && value == 1;
  }

  private <T> void applySourceSort(
      QueryWrapper<T> wrapper, String sort, String order, String priceColumn) {
    String column;
    if ("price".equals(sort)) {
      column = priceColumn;
    } else if ("createTime".equals(sort)) {
      column = "create_time";
    } else {
      column = "click_count";
    }
    wrapper.orderBy(true, "asc".equalsIgnoreCase(order), column).orderByAsc("id");
  }

  private String firstValue(Map<String, Object> params, String... keys) {
    for (String key : keys) {
      Object value = params.get(key);
      if (value != null && StringUtils.isNotBlank(value.toString())) {
        return value.toString().replace("%", "").trim();
      }
    }
    return null;
  }

  private Integer integerValue(Map<String, Object> params, String... keys) {
    String value = firstValue(params, keys);
    if (value == null) return null;
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Long longValue(Map<String, Object> params, String... keys) {
    String value = firstValue(params, keys);
    if (value == null) return null;
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private int positiveInteger(Object value, int defaultValue) {
    if (value == null) return defaultValue;
    try {
      int parsed = Integer.parseInt(value.toString());
      return parsed > 0 ? parsed : defaultValue;
    } catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  private long sortableValue(Object value) {
    if (value instanceof java.util.Date) {
      return ((java.util.Date) value).getTime();
    }
    return value instanceof Number ? ((Number) value).longValue() : Long.MIN_VALUE;
  }

  private int numberValue(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }

  private String joinLocation(String... parts) {
    List<String> values = new ArrayList<>();
    for (String part : parts) {
      if (StringUtils.isNotBlank(part) && !values.contains(part)) values.add(part);
    }
    return String.join(" · ", values);
  }

  @RequestMapping("/count")
  public R count(
      @RequestParam Map<String, Object> params,
      HomestayInfoEntity entity,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<HomestayInfoEntity> wrapper = new QueryWrapper<>();
    long count =
        homestayInfoService.count(
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(wrapper, entity), params), params));
    return R.ok().put("data", count);
  }
}
