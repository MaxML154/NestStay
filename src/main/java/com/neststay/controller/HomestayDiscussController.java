package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayDiscussEntity;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.ReviewInfoEntity;
import com.neststay.entity.view.HomestayDiscussView;
import com.neststay.service.ConsumerService;
import com.neststay.service.HomestayDiscussService;
import com.neststay.service.HomestayInfoService;
import com.neststay.service.HomestayRatingService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.ReviewInfoService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 民宿评论后端接口 */
@RestController
@RequestMapping("/homestayDiscuss")
public class HomestayDiscussController {
  @Autowired private HomestayDiscussService homestayDiscussService;
  @Autowired private HomestayInfoService homestayInfoService;
  @Autowired private HomestayRentalService homestayRentalService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private ReviewInfoService reviewInfoService;
  @Autowired private HomestayRatingService homestayRatingService;
  @Autowired private ConsumerService consumerService;

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      HomestayDiscussEntity homestayDiscuss,
      HttpServletRequest request) {
    QueryWrapper<HomestayDiscussEntity> ew = new QueryWrapper<>();
    PageUtils page =
        homestayDiscussService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayDiscuss), params), params));
    return R.ok().put("data", page);
  }

  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      HomestayDiscussEntity homestayDiscuss,
      HttpServletRequest request) {
    QueryWrapper<HomestayDiscussEntity> ew = new QueryWrapper<>();
    PageUtils page =
        homestayDiscussService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayDiscuss), params), params));
    return R.ok().put("data", page);
  }

  @RequestMapping("/lists")
  public R list(HomestayDiscussEntity homestayDiscuss) {
    QueryWrapper<HomestayDiscussEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(homestayDiscuss, "homestay_discuss"));
    return R.ok().put("data", homestayDiscussService.selectListView(ew));
  }

  @RequestMapping("/query")
  public R query(HomestayDiscussEntity homestayDiscuss) {
    QueryWrapper<HomestayDiscussEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(homestayDiscuss, "homestay_discuss"));
    HomestayDiscussView view = homestayDiscussService.selectView(ew);
    return R.ok("查询民宿评论成功").put("data", view);
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("data", homestayDiscussService.getById(id));
  }

  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    return R.ok().put("data", homestayDiscussService.getById(id));
  }

  @RequestMapping("/save")
  public R save(@RequestBody HomestayDiscussEntity homestayDiscuss, HttpServletRequest request) {
    return add(homestayDiscuss, request);
  }

  @RequestMapping("/canComment")
  public R canComment(@RequestParam Long refid, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) {
      return R.ok().put("data", false).put("reason", "请使用消费者账号登录后再评价");
    }
    HomestayRentalEntity rental = findCompletedOrder(refid, request);
    if (rental == null) {
      return R.ok().put("data", false).put("reason", "完成入住并退房后才能评论");
    }
    if (alreadyReviewed(rental.getOrderNumber())) {
      return R.ok().put("data", false).put("reason", "该订单已经评价过");
    }
    return R.ok().put("data", true);
  }

  @RequestMapping("/add")
  public R add(@RequestBody HomestayDiscussEntity homestayDiscuss, HttpServletRequest request) {
    if (homestayDiscuss == null) {
      return R.error(400, "评论不能为空");
    }
    if (!AuthSupport.isConsumer(request)) {
      return R.error(401, "请先登录后再评论");
    }
    homestayDiscuss.setUserId(AuthSupport.userId(request));
    if (homestayDiscuss.getRefid() == null) {
      return R.error(400, "民宿ID不能为空");
    }
    if (StringUtils.isBlank(homestayDiscuss.getContent())) {
      return R.error(400, "评论内容不能为空");
    }
    if (invalidScore(homestayDiscuss.getScoreStay())
        || invalidScore(homestayDiscuss.getScoreService())
        || invalidScore(homestayDiscuss.getScoreQuality())) {
      return R.error(400, "请为入住体验、商家服务、房源质量各打 1 至 5 分");
    }
    HomestayRentalEntity rental = findCompletedOrder(homestayDiscuss.getRefid(), request);
    if (rental == null) {
      return R.error(403, "完成入住并退房后才能评论");
    }
    if (alreadyReviewed(rental.getOrderNumber())) {
      return R.error(409, "该订单已经评价过");
    }
    double overall =
        (homestayDiscuss.getScoreStay()
                + homestayDiscuss.getScoreService()
                + homestayDiscuss.getScoreQuality())
            / 3.0;
    homestayDiscuss.setScore(Math.round(overall * 10.0) / 10.0);
    homestayDiscuss.setOrderNumber(rental.getOrderNumber());
    fillAuthor(homestayDiscuss, request);
    homestayDiscussService.save(homestayDiscuss);
    homestayRatingService.recalcListing(homestayDiscuss.getRefid());
    return R.ok();
  }

  private boolean invalidScore(Double score) {
    return score == null || score < 1 || score > 5;
  }

  private void fillAuthor(HomestayDiscussEntity discuss, HttpServletRequest request) {
    ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
    if (consumer == null) return;
    if (StringUtils.isBlank(discuss.getNickname())) {
      discuss.setNickname(
          StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
    }
    if (StringUtils.isBlank(discuss.getAvatarUrl())) {
      discuss.setAvatarUrl(consumer.getAvatar());
    }
  }

  private boolean alreadyReviewed(String orderNumber) {
    if (StringUtils.isBlank(orderNumber)) return false;
    if (homestayDiscussService.count(
            new QueryWrapper<HomestayDiscussEntity>().eq("order_number", orderNumber))
        > 0) {
      return true;
    }
    return reviewInfoService.count(
            new QueryWrapper<ReviewInfoEntity>().eq("order_number", orderNumber))
        > 0;
  }

  private HomestayRentalEntity findCompletedOrder(Long refid, HttpServletRequest request) {
    String account = AuthSupport.username(request);
    if (refid == null || StringUtils.isBlank(account)) return null;
    HomestayInfoEntity homestay = homestayInfoService.getById(refid);
    PlatformViewEntity platform =
        homestay == null ? platformViewService.getById(refid) : null;
    String homestayName = homestay != null ? homestay.getName() : null;
    if (StringUtils.isBlank(homestayName) && platform != null) {
      homestayName = platform.getHomestayName();
    }
    final String resolvedName = homestayName;
    QueryWrapper<HomestayRentalEntity> wrapper =
        new QueryWrapper<HomestayRentalEntity>()
            .eq("account", account)
            .in("order_status", "已完成", "已退房")
            .orderByDesc("id")
            .last("limit 1");
    wrapper.and(
        item -> {
          item.eq("homestay_id", refid);
          if (StringUtils.isNotBlank(resolvedName)) {
            item.or().eq("homestay_name", resolvedName);
          }
        });
    return homestayRentalService.getOne(wrapper, false);
  }

  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    HomestayDiscussEntity value =
        homestayDiscussService.getOne(new QueryWrapper<HomestayDiscussEntity>().eq("", username));
    return R.ok().put("data", value);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody HomestayDiscussEntity homestayDiscuss, HttpServletRequest request) {
    if (homestayDiscuss == null || homestayDiscuss.getId() == null) {
      return R.error(400, "评论ID不能为空");
    }
    homestayDiscussService.updateById(homestayDiscuss);
    HomestayDiscussEntity saved = homestayDiscussService.getById(homestayDiscuss.getId());
    if (saved != null) {
      homestayRatingService.recalcListing(saved.getRefid());
    }
    return R.ok();
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    if (ids == null || ids.length == 0) {
      return R.ok();
    }
    List<HomestayDiscussEntity> rows = homestayDiscussService.listByIds(Arrays.asList(ids));
    Set<Long> listingIds = new HashSet<>();
    for (HomestayDiscussEntity row : rows) {
      if (row != null && row.getRefid() != null) {
        listingIds.add(row.getRefid());
      }
    }
    homestayDiscussService.removeByIds(Arrays.asList(ids));
    for (Long listingId : listingIds) {
      homestayRatingService.recalcListing(listingId);
    }
    return R.ok();
  }

  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      HomestayDiscussEntity homestayDiscuss,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<HomestayDiscussEntity> ew = new QueryWrapper<>();
    Map<String, Object> newMap = new HashMap<>();
    Map<String, Object> param = new HashMap<>();
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String newKey = entry.getKey();
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    params.put("sort", "create_time");
    params.put("order", "desc");
    PageUtils page =
        homestayDiscussService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayDiscuss), params), params));
    return R.ok().put("data", page);
  }
}
