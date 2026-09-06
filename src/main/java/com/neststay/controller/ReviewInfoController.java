package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.ReviewInfoEntity;
import com.neststay.entity.view.ReviewInfoView;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.ReviewInfoService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价信息 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping("/reviewInfo")
public class ReviewInfoController {
  @Autowired private ReviewInfoService reviewInfoService;
  @Autowired private HomestayRentalService homestayRentalService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      ReviewInfoEntity reviewInfoEntity,
      HttpServletRequest request) {
    Object tableNameAttr = request.getSession().getAttribute("tableName");
    String tableName = tableNameAttr != null ? tableNameAttr.toString() : "";
    if (tableName.equals("merchant")) {
      reviewInfoEntity.setMerchantAccount((String) request.getSession().getAttribute("username"));
    }
    if (tableName.equals("consumer")) {
      reviewInfoEntity.setAccount((String) request.getSession().getAttribute("username"));
    }
    QueryWrapper<ReviewInfoEntity> ew = new QueryWrapper<ReviewInfoEntity>();

    PageUtils page =
        reviewInfoService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, reviewInfoEntity), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      ReviewInfoEntity reviewInfoEntity,
      HttpServletRequest request) {
    QueryWrapper<ReviewInfoEntity> ew = new QueryWrapper<ReviewInfoEntity>();

    PageUtils page =
        reviewInfoService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, reviewInfoEntity), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(ReviewInfoEntity reviewInfoEntity) {
    QueryWrapper<ReviewInfoEntity> ew = new QueryWrapper<ReviewInfoEntity>();
    ew.allEq(MPUtil.allEQMapPre(reviewInfoEntity, "review_info"));
    return R.ok().put("data", reviewInfoService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(ReviewInfoEntity reviewInfoEntity) {
    QueryWrapper<ReviewInfoEntity> ew = new QueryWrapper<ReviewInfoEntity>();
    ew.allEq(MPUtil.allEQMapPre(reviewInfoEntity, "review_info"));
    ReviewInfoView reviewInfoEntityView = reviewInfoService.selectView(ew);
    return R.ok("查询评价信息成功").put("data", reviewInfoEntityView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    ReviewInfoEntity reviewInfoEntity = reviewInfoService.getById(id);
    return R.ok().put("data", reviewInfoEntity);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    ReviewInfoEntity reviewInfoEntity = reviewInfoService.getById(id);
    return R.ok().put("data", reviewInfoEntity);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody ReviewInfoEntity reviewInfoEntity, HttpServletRequest request) {
    return add(reviewInfoEntity, request);
  }

  @RequestMapping("/add")
  public R add(@RequestBody ReviewInfoEntity reviewInfoEntity, HttpServletRequest request) {
    if (reviewInfoEntity == null) return R.error(400, "评价不能为空");
    if (!AuthSupport.isConsumer(request) && !AuthSupport.isAdmin(request)) {
      return R.error(401, "请先登录后再评价");
    }
    if (AuthSupport.isConsumer(request)) {
      String account = AuthSupport.username(request);
      reviewInfoEntity.setAccount(account);
      HomestayRentalEntity rental =
          homestayRentalService.getOne(
              new QueryWrapper<HomestayRentalEntity>()
                  .eq("account", account)
                  .eq(
                      StringUtils.isNotBlank(reviewInfoEntity.getOrderNumber())
                          ? "order_number"
                          : "homestay_name",
                      StringUtils.defaultIfBlank(
                          reviewInfoEntity.getOrderNumber(), reviewInfoEntity.getHomestayName()))
                  .in("order_status", "已完成", "已退房")
                  .last("limit 1"),
              false);
      if (rental == null) {
        return R.error(403, "完成入住并退房后才能评价");
      }
      if (reviewInfoService.count(
              new QueryWrapper<ReviewInfoEntity>().eq("order_number", rental.getOrderNumber()))
          > 0) {
        return R.error(409, "该订单已经评价过");
      }
      reviewInfoEntity.setOrderNumber(rental.getOrderNumber());
    }
    reviewInfoService.save(reviewInfoEntity);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody ReviewInfoEntity reviewInfoEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(reviewInfoEntity);
    reviewInfoService.updateById(reviewInfoEntity); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    reviewInfoService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
