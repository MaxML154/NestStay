package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.PlatformViewView;
import com.neststay.service.FavoriteService;
import com.neststay.service.HomestayIndexService;
import com.neststay.service.HomestayTagService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.impl.HomestayIndexServiceImpl;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

/** 平台民宿信息后端接口 */
@RestController
@RequestMapping("/platformView")
public class PlatformViewController {
  @Autowired private PlatformViewService platformViewService;
  @Autowired private FavoriteService storeupService;
  @Autowired private HomestayTagService homestayTagService;
  @Autowired private HomestayIndexService homestayIndexService;

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      PlatformViewEntity platformView,
      @RequestParam(required = false) Double priceStart,
      @RequestParam(required = false) Double priceEnd,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    applyMerchantScope(platformView, request);
    QueryWrapper<PlatformViewEntity> ew = priceWrapper(priceStart, priceEnd);
    PageUtils data = query(params, platformView, ew);
    markSuggested(data);
    return R.ok().put("data", data);
  }

  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      PlatformViewEntity platformView,
      @RequestParam(required = false) Double priceStart,
      @RequestParam(required = false) Double priceEnd,
      HttpServletRequest request) {
    QueryWrapper<PlatformViewEntity> wrapper = priceWrapper(priceStart, priceEnd);
    wrapper.and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"));
    return R.ok().put("data", query(params, platformView, wrapper));
  }

  private PageUtils query(
      Map<String, Object> params,
      PlatformViewEntity platformView,
      QueryWrapper<PlatformViewEntity> ew) {
    if (params.get("homestayName") != null) {
      ew.like("homestay_name", params.get("homestayName").toString().replace("%", ""));
      params.remove("homestayName");
    }
    if (params.get("homestayLocation") != null) {
      ew.like("homestay_location", params.get("homestayLocation").toString().replace("%", ""));
      params.remove("homestayLocation");
    }
    if (params.get("layout") != null) {
      ew.like("layout", params.get("layout").toString().replace("%", ""));
      params.remove("layout");
    }
    return platformViewService.queryPage(
        params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, platformView), params), params));
  }

  private QueryWrapper<PlatformViewEntity> priceWrapper(Double start, Double end) {
    QueryWrapper<PlatformViewEntity> wrapper = new QueryWrapper<>();
    if (start != null) wrapper.ge("price_per_day", start);
    if (end != null) wrapper.le("price_per_day", end);
    return wrapper;
  }

  private void applyMerchantScope(PlatformViewEntity entity, HttpServletRequest request) {
    Object tableName = request.getSession().getAttribute("tableName");
    Object username = request.getSession().getAttribute("username");
    if ("merchant".equals(String.valueOf(tableName)) && username != null) {
      entity.setMerchantAccount(username.toString());
    }
  }

  @RequestMapping("/lists")
  public R lists(PlatformViewEntity platformView, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<PlatformViewEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(platformView, "platform_view"));
    return R.ok().put("data", platformViewService.selectListView(ew));
  }

  @RequestMapping("/query")
  public R queryView(PlatformViewEntity platformView, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<PlatformViewEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(platformView, "platform_view"));
    return R.ok("查询平台民宿信息成功").put("data", platformViewService.selectView(ew));
  }

  @RequestMapping({"/info/{id}", "/detail/{id}"})
  @IgnoreAuth
  public R detail(@PathVariable("id") Long id) {
    PlatformViewEntity entity = platformViewService.getById(id);
    if (entity == null) return R.error(404, "平台民宿不存在");
    entity.setClickCount(entity.getClickCount() == null ? 1 : entity.getClickCount() + 1);
    entity.setClickTime(new java.util.Date());
    platformViewService.updateById(entity);
    PlatformViewView view =
        platformViewService.selectView(new QueryWrapper<PlatformViewEntity>().eq("id", id));
    if (view != null) {
      view.setTagIds(homestayTagService.tagIdsForListing(id));
    }
    return R.ok().put("data", view);
  }

  @RequestMapping("/save")
  public R save(@RequestBody PlatformViewEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    if (entity == null || StringUtils.isBlank(entity.getHomestayName())) {
      return R.error(400, "民宿名称不能为空");
    }
    if (AuthSupport.isMerchant(request)) {
      entity.setMerchantAccount(AuthSupport.username(request));
      entity.setAuditStatus("待审核");
    } else if (StringUtils.isBlank(entity.getAuditStatus())) {
      entity.setAuditStatus("已通过");
    }
    if (entity.getVideoIntro() == null) {
      entity.setVideoIntro("");
    }
    if (StringUtils.isBlank(entity.getHomestayImage())) {
      return R.error(400, "请上传民宿图片");
    }
    platformViewService.save(entity);
    persistListingTags(entity);
    return R.ok();
  }

  @RequestMapping("/add")
  public R add(@RequestBody PlatformViewEntity entity, HttpServletRequest request) {
    return save(entity, request);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody PlatformViewEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    if (entity == null || entity.getId() == null) {
      return R.error(400, "平台民宿ID不能为空");
    }
    PlatformViewEntity existing = platformViewService.getById(entity.getId());
    if (existing == null) {
      return R.error(404, "平台民宿不存在");
    }
    if (AuthSupport.isMerchant(request)
        && !StringUtils.equals(existing.getMerchantAccount(), AuthSupport.username(request))) {
      return R.error(403, "无权修改其他商家的民宿");
    }
    if (AuthSupport.isMerchant(request)) {
      entity.setMerchantAccount(existing.getMerchantAccount());
      entity.setAuditStatus("待审核");
    }
    platformViewService.updateById(entity);
    persistListingTags(entity);
    return R.ok();
  }

  @RequestMapping("/audit")
  public R audit(@RequestBody PlatformViewEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || entity.getId() == null || StringUtils.isBlank(entity.getAuditStatus())) {
      return R.error(400, "缺少审核结果");
    }
    PlatformViewEntity existing = platformViewService.getById(entity.getId());
    if (existing == null) return R.error(404, "平台民宿不存在");
    existing.setAuditStatus(entity.getAuditStatus());
    platformViewService.updateById(existing);
    return R.ok().put("data", existing);
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    if (AuthSupport.isMerchant(request)) {
      long selected =
          platformViewService.count(
              new QueryWrapper<PlatformViewEntity>().in("id", Arrays.asList(ids)));
      long owned =
          platformViewService.count(
              new QueryWrapper<PlatformViewEntity>()
                  .in("id", Arrays.asList(ids))
                  .eq("merchant_account", AuthSupport.username(request)));
      if (selected != owned) return R.error(403, "无权删除其他商家的民宿");
    }
    homestayTagService.clearListingTags(Arrays.asList(ids));
    platformViewService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private void markSuggested(PageUtils data) {
    if (data == null || data.getList() == null) return;
    for (Object row : data.getList()) {
      if (row instanceof PlatformViewEntity) {
        PlatformViewEntity listing = (PlatformViewEntity) row;
        listing.setSuggestedSelected(HomestayIndexServiceImpl.suggested(listing));
      }
    }
  }

  private void persistListingTags(PlatformViewEntity entity) {
    if (entity == null || entity.getId() == null || entity.getTagIds() == null) return;
    homestayTagService.replaceListingTags(entity.getId(), entity.getTagIds());
  }

  @RequestMapping("/select")
  public R toggleSelected(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (body == null || body.get("id") == null) return R.error(400, "民宿ID不能为空");
    Long id;
    try {
      id = Long.valueOf(body.get("id").toString());
    } catch (NumberFormatException exception) {
      return R.error(400, "民宿ID无效");
    }
    PlatformViewEntity existing = platformViewService.getById(id);
    if (existing == null) return R.error(404, "平台民宿不存在");
    Object selected = body.get("selected");
    if (selected == null) {
      existing.setIsSelected(existing.getIsSelected() != null && existing.getIsSelected() == 1 ? 0 : 1);
    } else {
      String text = selected.toString();
      existing.setIsSelected("1".equals(text) || "true".equalsIgnoreCase(text) ? 1 : 0);
    }
    existing.setSelectLock(1);
    platformViewService.updateById(existing);
    return R.ok().put("data", existing);
  }

  @RequestMapping("/index/recalc")
  public R recalcIndex(HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    int n = homestayIndexService.recalcAll();
    return R.ok().put("data", n);
  }

  @RequestMapping("/index/candidates")
  public R indexCandidates(HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    List<PlatformViewEntity> out = new java.util.ArrayList<>();
    for (PlatformViewEntity listing : platformViewService.list()) {
      listing.setSuggestedSelected(HomestayIndexServiceImpl.suggested(listing));
      if (Boolean.TRUE.equals(listing.getSuggestedSelected())
          && (listing.getIsSelected() == null || listing.getIsSelected() != 1)) {
        out.add(listing);
      }
    }
    out.sort(
        (a, b) ->
            Double.compare(
                b.getPlatformIndex() == null ? 0 : b.getPlatformIndex(),
                a.getPlatformIndex() == null ? 0 : a.getPlatformIndex()));
    return R.ok().put("data", out);
  }

  @RequestMapping("/index/adoptSuggested")
  public R adoptSuggested(HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    int n = 0;
    for (PlatformViewEntity listing : platformViewService.list()) {
      if (!HomestayIndexServiceImpl.suggested(listing)) continue;
      if (listing.getIsSelected() != null && listing.getIsSelected() == 1) continue;
      listing.setIsSelected(1);
      listing.setSelectLock(1);
      platformViewService.updateById(listing);
      n++;
    }
    return R.ok().put("data", n);
  }

  @RequestMapping("/count")
  public R count(
      @RequestParam Map<String, Object> params,
      PlatformViewEntity entity,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    applyMerchantScope(entity, request);
    QueryWrapper<PlatformViewEntity> wrapper = new QueryWrapper<>();
    long count =
        platformViewService.count(
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(wrapper, entity), params), params));
    return R.ok().put("data", count);
  }

  @RequestMapping("/group/{columnName}")
  public R group(@PathVariable("columnName") String columnName, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    String originalColumnName = columnName;
    columnName = MPUtil.normalizeColumnName(PlatformViewEntity.class, columnName);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("column", columnName);
    PlatformViewEntity scope = new PlatformViewEntity();
    applyMerchantScope(scope, request);
    QueryWrapper<PlatformViewEntity> ew = new QueryWrapper<>();
    MPUtil.likeOrEq(ew, scope);
    List<Map<String, Object>> result = platformViewService.selectGroup(params, ew);
    MPUtil.aliasMapKey(result, originalColumnName, columnName);
    formatStatDates(result);
    return R.ok().put("data", result);
  }

  @RequestMapping("/value/{xColumnName}/{yColumnName}")
  public R value(
      @PathVariable("yColumnName") String yColumnName,
      @PathVariable("xColumnName") String xColumnName,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    String originalXColumnName = xColumnName;
    xColumnName = MPUtil.normalizeColumnName(PlatformViewEntity.class, xColumnName);
    yColumnName = MPUtil.normalizeColumnName(PlatformViewEntity.class, yColumnName);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("xColumn", xColumnName);
    params.put("yColumn", yColumnName);
    PlatformViewEntity scope = new PlatformViewEntity();
    applyMerchantScope(scope, request);
    QueryWrapper<PlatformViewEntity> ew = new QueryWrapper<>();
    MPUtil.likeOrEq(ew, scope);
    List<Map<String, Object>> result = platformViewService.selectValue(params, ew);
    MPUtil.aliasMapKey(result, originalXColumnName, xColumnName);
    formatStatDates(result);
    return R.ok().put("data", result);
  }

  private void formatStatDates(List<Map<String, Object>> rows) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    for (Map<String, Object> row : rows) {
      for (String key : row.keySet()) {
        if (row.get(key) instanceof Date) {
          row.put(key, sdf.format((Date) row.get(key)));
        }
      }
    }
  }

  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      PlatformViewEntity platformView,
      HttpServletRequest request,
      String pre) {
    params.put("sort", "click_count");
    params.put("order", "desc");
    return R.ok().put("data", query(params, platformView, new QueryWrapper<>()));
  }

  @RequestMapping("/autoSort2")
  public R autoSort2(
      @RequestParam Map<String, Object> params,
      PlatformViewEntity platformView,
      HttpServletRequest request) {
    Object userId = request.getSession().getAttribute("userId");
    if (userId == null) return R.error(401, "未登录");
    List<FavoriteEntity> favorites =
        storeupService.list(
            new QueryWrapper<FavoriteEntity>()
                .eq("type", 1)
                .eq("user_id", userId)
                .eq("table_name", "platform_view")
                .orderByDesc("create_time"));
    params.put("sort", "id");
    params.put("order", "desc");
    PageUtils page = query(params, platformView, new QueryWrapper<>());
    return R.ok().put("data", page);
  }
}
