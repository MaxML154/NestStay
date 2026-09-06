package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.FavoriteView;
import com.neststay.service.FavoriteService;
import com.neststay.service.HomestayInfoService;
import com.neststay.service.NewsArticleService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RecommendEventService;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 收藏表后端接口。 */
@RestController
@RequestMapping({"/favorite", "/storeup"})
public class FavoriteController {
  @Autowired private FavoriteService storeupService;
  @Autowired private HomestayInfoService homestayInfoService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private NewsArticleService newsArticleService;
  @Autowired private RecommendEventService recommendEventService;

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      FavoriteEntity storeup,
      HttpServletRequest request) {
    if (isConsumer(request)) {
      Long userId = currentUserId(request);
      if (userId == null) return authError(request);
      storeup.setUserId(userId);
    } else if (!isAdmin(request)) {
      return authError(request);
    }
    return R.ok().put("data", queryPage(params, storeup));
  }

  /** 消费者端只允许读取当前登录用户的收藏。 */
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      FavoriteEntity storeup,
      HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId != null) {
      storeup.setUserId(userId);
    } else if (isConsumer(request)) {
      return authError(request);
    } else if (!isAdmin(request)) {
      return authError(request);
    }
    if (storeup.getTableName() == null && params.get("tablename") != null) {
      storeup.setTableName(params.get("tablename").toString());
    }
    if (storeup.getUserId() == null && isAdmin(request) && params.get("userid") != null) {
      storeup.setUserId(Long.valueOf(params.get("userid").toString()));
    }
    return R.ok().put("data", queryPage(params, storeup));
  }

  @RequestMapping("/lists")
  public R lists(FavoriteEntity storeup) {
    QueryWrapper<FavoriteEntity> wrapper = new QueryWrapper<>();
    wrapper.allEq(MPUtil.allEQMapPre(storeup, "favorite"));
    return R.ok().put("data", storeupService.selectListView(wrapper));
  }

  @RequestMapping("/query")
  public R query(FavoriteEntity storeup) {
    QueryWrapper<FavoriteEntity> wrapper = new QueryWrapper<>();
    wrapper.allEq(MPUtil.allEQMapPre(storeup, "favorite"));
    FavoriteView storeupView = storeupService.selectView(wrapper);
    return R.ok("查询收藏表成功").put("data", storeupView);
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("data", storeupService.getById(id));
  }

  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    if (isAdmin(request)) {
      FavoriteEntity favorite = storeupService.getById(id);
      return favorite == null ? R.error(404, "收藏记录不存在") : R.ok().put("data", favorite);
    }
    Long userId = currentConsumerId(request);
    if (userId == null) return authError(request);
    FavoriteEntity favorite =
        storeupService.getOne(
            new QueryWrapper<FavoriteEntity>().eq("id", id).eq("user_id", userId));
    return favorite == null ? R.error(404, "收藏记录不存在") : R.ok().put("data", favorite);
  }

  @RequestMapping("/save")
  public R save(@RequestBody FavoriteEntity storeup, HttpServletRequest request) {
    return add(storeup, request);
  }

  @RequestMapping("/add")
  @Transactional
  public R add(@RequestBody FavoriteEntity storeup, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId == null) return authError(request);
    if (storeup == null
        || storeup.getRefid() == null
        || StringUtils.isBlank(storeup.getTableName())) {
      return R.error(400, "收藏对象不能为空");
    }
    String tableName = storeup.getTableName().trim().toLowerCase();
    String type = StringUtils.defaultIfBlank(storeup.getType(), "1").trim();
    if (!isSupportedFavorite(tableName, type)) {
      return R.error(400, "不支持的收藏类型");
    }
    if (StringUtils.isBlank(storeup.getName())) {
      return R.error(400, "收藏名称不能为空");
    }
    storeup.setUserId(userId);
    storeup.setTableName(tableName);
    storeup.setType(type);
    if (storeupService.count(
            new QueryWrapper<FavoriteEntity>()
                .eq("user_id", userId)
                .eq("refid", storeup.getRefid())
                .eq("table_name", tableName)
                .eq("type", type))
        > 0) {
      return R.error(409, "已经收藏过了");
    }
    try {
      storeupService.save(storeup);
    } catch (DuplicateKeyException exception) {
      return R.error(409, "已经收藏过了");
    }
    adjustTargetCount(storeup, 1);
    if ("platform_view".equals(tableName) && "1".equals(type)) {
      recommendEventService.record(userId, null, storeup.getRefid(), "favorite", null);
    }
    return R.ok().put("data", storeup);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody FavoriteEntity storeup, HttpServletRequest request) {
    if (storeup == null || storeup.getId() == null) {
      return R.error(400, "收藏记录ID不能为空");
    }
    storeup.setUserId(null);
    if (isAdmin(request)) {
      return storeupService.updateById(storeup) ? R.ok() : R.error(404, "收藏记录不存在");
    }
    Long userId = currentConsumerId(request);
    if (userId == null) return authError(request);
    boolean updated =
        storeupService.update(
            storeup,
            new QueryWrapper<FavoriteEntity>().eq("id", storeup.getId()).eq("user_id", userId));
    return updated ? R.ok() : R.error(404, "收藏记录不存在");
  }

  @RequestMapping("/delete")
  @Transactional
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (ids == null || ids.length == 0) {
      return R.error(400, "请选择要取消的收藏");
    }
    QueryWrapper<FavoriteEntity> wrapper =
        new QueryWrapper<FavoriteEntity>().in("id", Arrays.asList(ids));
    if (!isAdmin(request)) {
      Long userId = currentConsumerId(request);
      if (userId == null) return authError(request);
      wrapper.eq("user_id", userId);
    }
    List<FavoriteEntity> records = storeupService.list(wrapper);
    if (records.isEmpty()) {
      return R.error(404, "收藏记录不存在");
    }
    storeupService.removeByIds(
        records.stream().map(FavoriteEntity::getId).collect(Collectors.toList()));
    for (FavoriteEntity record : records) {
      adjustTargetCount(record, -1);
    }
    return R.ok();
  }

  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      FavoriteEntity storeup,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<FavoriteEntity> wrapper = new QueryWrapper<>();
    Map<String, Object> newMap = new HashMap<>();
    Map<String, Object> param = new HashMap<>();
    Iterator<Map.Entry<String, Object>> iterator = param.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String newKey = entry.getKey();
      if (pre != null && pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    params.put("sort", "create_time");
    params.put("order", "desc");
    return R.ok().put("data", queryPage(params, storeup, wrapper));
  }

  private PageUtils queryPage(Map<String, Object> params, FavoriteEntity storeup) {
    return queryPage(params, storeup, new QueryWrapper<>());
  }

  private PageUtils queryPage(
      Map<String, Object> params, FavoriteEntity storeup, QueryWrapper<FavoriteEntity> wrapper) {
    return storeupService.queryPage(
        params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(wrapper, storeup), params), params));
  }

  private Long currentUserId(HttpServletRequest request) {
    Object userId = request.getSession().getAttribute("userId");
    if (userId == null) {
      return null;
    }
    try {
      return Long.valueOf(userId.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Long currentConsumerId(HttpServletRequest request) {
    return isConsumer(request) ? currentUserId(request) : null;
  }

  private boolean isConsumer(HttpServletRequest request) {
    return "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")));
  }

  private boolean isAdmin(HttpServletRequest request) {
    return "管理员".equals(String.valueOf(request.getSession().getAttribute("role")));
  }

  private R authError(HttpServletRequest request) {
    return currentUserId(request) == null ? R.error(401, "请先登录") : R.error(403, "仅消费者或管理员可操作收藏");
  }

  private boolean isSupportedFavorite(String tableName, String type) {
    if ("homestay_info".equals(tableName) || "platform_view".equals(tableName)) {
      return "1".equals(type);
    }
    if ("forum".equals(tableName)) {
      return "1".equals(type) || "21".equals(type);
    }
    return "news".equals(tableName) && ("1".equals(type) || "21".equals(type));
  }

  /** 收藏/点赞成功后同步目标表计数，避免消费者再去调用民宿更新接口。 */
  private void adjustTargetCount(FavoriteEntity favorite, int delta) {
    if (favorite == null
        || favorite.getRefid() == null
        || StringUtils.isBlank(favorite.getTableName())) {
      return;
    }
    String tableName = favorite.getTableName().trim().toLowerCase();
    String type = StringUtils.defaultIfBlank(favorite.getType(), "1").trim();
    if ("1".equals(type) && "homestay_info".equals(tableName)) {
      homestayInfoService.update(
          new UpdateWrapper<HomestayInfoEntity>()
              .eq("id", favorite.getRefid())
              .setSql(bumpSql("favorite_count", delta)));
    } else if ("1".equals(type) && "platform_view".equals(tableName)) {
      platformViewService.update(
          new UpdateWrapper<PlatformViewEntity>()
              .eq("id", favorite.getRefid())
              .setSql(bumpSql("favorite_count", delta)));
    } else if ("news".equals(tableName)) {
      String column = "21".equals(type) ? "thumbs_up_count" : "favorite_count";
      if ("1".equals(type) || "21".equals(type)) {
        newsArticleService.update(
            new UpdateWrapper<NewsArticleEntity>()
                .eq("id", favorite.getRefid())
                .setSql(bumpSql(column, delta)));
      }
    }
  }

  private String bumpSql(String column, int delta) {
    int step = Math.abs(delta);
    String operator = delta >= 0 ? "+" : "-";
    return column
        + " = GREATEST(IFNULL("
        + column
        + ", 0) "
        + operator
        + " "
        + step
        + ", 0)";
  }
}
