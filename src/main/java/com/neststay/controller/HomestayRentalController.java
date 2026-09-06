package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.HomestayRentalView;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.MerchantStayLookup;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RefundRequestService;
import com.neststay.service.UnpaidOrderTimeoutService;
import com.neststay.service.RecommendEventService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.PrivacyMask;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 民宿租赁 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@RestController
@RequestMapping("/homestayRental")
public class HomestayRentalController {
  @Autowired private HomestayRentalService homestayRentalService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private MerchantStayLookup merchantStayLookup;
  @Autowired private ConsumerService consumerService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private UnpaidOrderTimeoutService unpaidOrderTimeoutService;
  @Autowired private RecommendEventService recommendEventService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      HomestayRentalEntity homestayRental,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)
        && !AuthSupport.isMerchant(request)
        && !AuthSupport.isConsumer(request)) {
      return R.error(401, "请先登录");
    }
    Object tableNameAttr = request.getSession().getAttribute("tableName");
    if (tableNameAttr != null) {
      String tableName = tableNameAttr.toString();
      if (tableName.equals("merchant")) {
        homestayRental.setMerchantAccount((String) request.getSession().getAttribute("username"));
      }
      if (tableName.equals("consumer")) {
        homestayRental.setAccount((String) request.getSession().getAttribute("username"));
      }
    }
    normalizeApprovalFilter(homestayRental);
    QueryWrapper<HomestayRentalEntity> ew = new QueryWrapper<HomestayRentalEntity>();

    PageUtils page =
        homestayRentalService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayRental), params), params));
    if (AuthSupport.isAdmin(request) || AuthSupport.isMerchant(request)) {
      maskRentalPage(page);
    }
    attachRefundStatus(page);
    resolveListings(page);
    return R.ok().put("data", page);
  }

  /** 旧列表接口，按当前登录身份限定范围。 */
  @RequestMapping("/legacyList")
  public R list(
      @RequestParam Map<String, Object> params,
      HomestayRentalEntity homestayRental,
      HttpServletRequest request) {
    return page(params, homestayRental, request);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(HomestayRentalEntity homestayRental, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    applyOwnerScope(homestayRental, request);
    QueryWrapper<HomestayRentalEntity> ew = new QueryWrapper<HomestayRentalEntity>();
    ew.allEq(MPUtil.allEQMapPre(homestayRental, "homestay_rental"));
    List<HomestayRentalView> rows = homestayRentalService.selectListView(ew);
    if (AuthSupport.isAdmin(request) || AuthSupport.isMerchant(request)) {
      for (HomestayRentalView row : rows) maskRentalRow(row);
    }
    return R.ok().put("data", rows);
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(HomestayRentalEntity homestayRental, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    applyOwnerScope(homestayRental, request);
    QueryWrapper<HomestayRentalEntity> ew = new QueryWrapper<HomestayRentalEntity>();
    ew.allEq(MPUtil.allEQMapPre(homestayRental, "homestay_rental"));
    HomestayRentalView homestayRentalView = homestayRentalService.selectView(ew);
    if (homestayRentalView == null) return R.error(404, "订单不存在");
    HomestayRentalEntity order = homestayRentalService.getById(homestayRentalView.getId());
    if (order == null || !canAccessOrder(order, request)) return R.error(404, "订单不存在");
    if (AuthSupport.isAdmin(request) || AuthSupport.isMerchant(request)) {
      maskRentalRow(homestayRentalView);
    }
    return R.ok("查询民宿租赁成功").put("data", homestayRentalView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    return readOrder(id, request);
  }

  /** 前端详情，仅本人、所属商家或管理员可读。 */
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    return readOrder(id, request);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody HomestayRentalEntity homestayRental, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    // ValidatorUtils.validateEntity(homestayRental);
    homestayRentalService.save(homestayRental);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/legacyAdd")
  public R add(@RequestBody HomestayRentalEntity homestayRental, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    String username = currentConsumerUsername(request);
    if (userId == null || username == null) return consumerAuthError(request);
    if (homestayRental == null
        || homestayRental.getId() != null
        || homestayRental.getOrderNumber() == null
        || homestayRental.getHomestayName() == null
        || homestayRental.getPricePerDay() == null
        || homestayRental.getBookingDays() == null
        || homestayRental.getBookingDays() < 1) {
      return R.error(400, "订单信息不完整");
    }
    homestayRental.setAccount(username);
    homestayRental.setTotalPrice(homestayRental.getPricePerDay() * homestayRental.getBookingDays());
    if (homestayRental.getRentalTime() == null) {
      homestayRental.setRentalTime(new Date());
    }
    if (homestayRental.getApprovalStatus() == null) {
      homestayRental.setApprovalStatus("待审核");
    }
    homestayRentalService.save(homestayRental);
    return R.ok();
  }

  /** 管理员或商家后台修改订单，消费者必须使用专用状态接口。 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody HomestayRentalEntity homestayRental, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
    if ("consumer".equals(tableName)) {
      return R.error(403, "消费者不能直接修改订单");
    }
    if (homestayRental == null || homestayRental.getId() == null) {
      return R.error(400, "订单ID不能为空");
    }
    HomestayRentalEntity existing = homestayRentalService.getById(homestayRental.getId());
    if (existing == null) return R.error(404, "订单不存在");
    if ("merchant".equals(tableName)
        && !String.valueOf(request.getSession().getAttribute("username"))
            .equals(existing.getMerchantAccount())) {
      return R.error(403, "无权修改该订单");
    }
    homestayRental.setAccount(null);
    homestayRental.setTotalPrice(null);
    homestayRental.setPricePerDay(null);
    homestayRental.setSourceType(null);
    homestayRental.setHomestayId(null);
    homestayRental.setIsPaid(null);
    homestayRental.setPayDeadline(null);
    homestayRental.setMerchantReviewedAt(null);
    homestayRental.setMerchantCheckinAt(null);
    homestayRental.setMerchantCheckoutAt(null);
    homestayRentalService.updateById(homestayRental);
    stampMerchantReviewedIfNeeded(homestayRentalService.getById(homestayRental.getId()));
    return R.ok();
  }

  /** 审核 */
  @RequestMapping("/shBatch")
  @Transactional
  public R update(
      @RequestBody Long[] ids,
      @RequestParam(required = false) String approvalStatus,
      @RequestParam(required = false) String approvalReply,
      @RequestParam(required = false) String sfsh,
      @RequestParam(required = false) String shhf,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    approvalStatus = approvalStatus == null ? sfsh : approvalStatus;
    approvalReply = approvalReply == null ? shhf : approvalReply;
    if (approvalStatus == null) return R.error(400, "审核状态不能为空");
    List<HomestayRentalEntity> list = new ArrayList<HomestayRentalEntity>();
    for (Long id : ids) {
      HomestayRentalEntity homestayRental = homestayRentalService.getById(id);
      if (homestayRental == null) continue;
      if (AuthSupport.isMerchant(request)
          && !String.valueOf(AuthSupport.username(request))
              .equals(homestayRental.getMerchantAccount())) {
        return R.error(403, "无权审核其他商家的订单");
      }
      homestayRental.setApprovalStatus(approvalStatus);
      homestayRental.setApprovalReply(approvalReply);
      if ("是".equals(approvalStatus) || "已通过".equals(approvalStatus)) {
        homestayRental.setApprovalStatus("已通过");
        homestayRental.setOrderStatus(
            "已支付".equals(homestayRental.getIsPaid()) ? "待入住" : "待支付");
        unpaidOrderTimeoutService.startPayWindow(homestayRental);
      } else if ("否".equals(approvalStatus) || "已拒绝".equals(approvalStatus)) {
        homestayRental.setApprovalStatus("已拒绝");
        homestayRental.setOrderStatus("已拒绝");
      }
      if (isDecidedApproval(homestayRental.getApprovalStatus())
          && homestayRental.getMerchantReviewedAt() == null) {
        homestayRental.setMerchantReviewedAt(new Date());
      }
      list.add(homestayRental);
    }
    homestayRentalService.updateBatchById(list);
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    homestayRentalService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  /** 消费者端订单列表，只允许读取当前账号。 */
  @RequestMapping("/list")
  public R consumerList(
      @RequestParam Map<String, Object> params,
      HomestayRentalEntity order,
      HttpServletRequest request) {
    String username = currentConsumerUsername(request);
    if (username == null) return consumerAuthError(request);
    order.setAccount(username);
    PageUtils page =
        homestayRentalService.queryPage(
            params,
            MPUtil.sort(
                MPUtil.between(
                    MPUtil.likeOrEq(new QueryWrapper<HomestayRentalEntity>(), order), params),
                params));
    attachRefundStatus(page);
    resolveListings(page);
    return R.ok().put("data", page);
  }

  /** 消费者下单：民宿信息、单价和总价全部由服务端确认。 */
  @RequestMapping("/add")
  @Transactional
  public R createOrder(@RequestBody HomestayRentalEntity order, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    String username = currentConsumerUsername(request);
    if (userId == null || username == null) return consumerAuthError(request);
    if (order == null
        || order.getHomestayId() == null
        || !isSupportedSource(order.getSourceType())
        || order.getCheckInDate() == null
        || order.getCheckOutDate() == null
        || order.getGuestCount() == null
        || order.getGuestCount() < 1) {
      return R.error(400, "请完整填写入住日期、退房日期和入住人数");
    }

    LocalDate checkIn = toLocalDate(order.getCheckInDate());
    LocalDate checkOut = toLocalDate(order.getCheckOutDate());
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
    if (checkIn.isBefore(today) || !checkOut.isAfter(checkIn)) {
      return R.error(400, "入住日期不能早于今天，退房日期必须晚于入住日期");
    }
    long days = ChronoUnit.DAYS.between(checkIn, checkOut);
    if (days > 365) return R.error(400, "单次预订不能超过365天");

    R sourceResult = fillOrderFromSource(order);
    if (((Number) sourceResult.get("code")).intValue() != 0) return sourceResult;
    ConsumerEntity consumer = consumerService.getById(userId);
    if (consumer == null) return R.error(404, "用户信息不存在");

    order.setId(null);
    order.setOrderNumber(
        "NS"
            + System.currentTimeMillis()
            + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
    order.setBookingDays((int) days);
    order.setTotalPrice(Math.multiplyExact(order.getPricePerDay(), (int) days));
    order.setRentalTime(new Date());
    order.setAccount(username);
    order.setRealName(consumer.getRealName());
    order.setPhone(consumer.getPhone());
    order.setApprovalStatus("待审核");
    order.setApprovalReply(null);
    order.setIsPaid("未支付");
    order.setPayDeadline(null);
    order.setOrderStatus("待审核");

    long duplicate =
        homestayRentalService.count(
            new QueryWrapper<HomestayRentalEntity>()
                .eq("account", username)
                .eq("source_type", order.getSourceType())
                .eq("homestay_id", order.getHomestayId())
                .lt("check_in_date", order.getCheckOutDate())
                .gt("check_out_date", order.getCheckInDate())
                .notIn("order_status", Arrays.asList("已取消", "已拒绝")));
    if (duplicate > 0) return R.error(409, "该日期范围内已有相同民宿订单");

    try {
      homestayRentalService.save(order);
    } catch (ArithmeticException exception) {
      return R.error(400, "订单金额超出允许范围");
    }
    if ("platform_view".equals(order.getSourceType())) {
      recommendEventService.record(userId, null, order.getHomestayId(), "order", null);
    }
    return R.ok().put("data", order);
  }

  /** 当前登录用户可将本人订单、所属商家订单或任意管理员订单标记为已支付。 */
  @RequestMapping("/pay/{id}")
  @Transactional
  public R pay(@PathVariable Long id, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    HomestayRentalEntity order = homestayRentalService.getById(id);
    if (order == null || !canAccessOrder(order, request)) return R.error(404, "订单不存在");
    if ("已取消".equals(order.getOrderStatus()) || "已拒绝".equals(order.getOrderStatus())) {
      return R.error(409, "当前订单状态不可支付");
    }
    if (!isApproved(order.getApprovalStatus())) {
      return R.error(409, "请审核通过后再支付");
    }
    if (unpaidOrderTimeoutService.expireIfDue(order)) {
      return R.error(409, "超过2小时未支付，预订已自动取消");
    }
    if ("已支付".equals(order.getIsPaid())) return R.ok().put("data", order);
    if ("已退款".equals(order.getIsPaid())) return R.error(409, "该订单已退款");
    order.setIsPaid("已支付");
    if (isApproved(order.getApprovalStatus())) order.setOrderStatus("待入住");
    homestayRentalService.updateById(order);
    return R.ok().put("data", order);
  }

  /** 未入住且未完成的本人订单可以取消。 */
  @RequestMapping("/cancel/{id}")
  @Transactional
  public R cancel(@PathVariable Long id, HttpServletRequest request) {
    if (currentConsumerUsername(request) == null) return consumerAuthError(request);
    HomestayRentalEntity order = currentConsumerOrder(id, request);
    if (order == null) return R.error(404, "订单不存在");
    if (Arrays.asList("已入住", "已完成", "已取消").contains(order.getOrderStatus())) {
      return R.error(409, "当前订单状态不可取消");
    }
    order.setOrderStatus("已取消");
    order.setApprovalStatus("已取消");
    homestayRentalService.updateById(order);
    return R.ok().put("data", order);
  }

  /** （按值统计） */
  @RequestMapping("/value/{xColumnName}/{yColumnName}")
  public R value(
      @PathVariable("yColumnName") String yColumnName,
      @PathVariable("xColumnName") String xColumnName,
      HttpServletRequest request) {
    R denied = requireLogin(request);
    if (denied != null) return denied;
    String originalXColumnName = xColumnName;
    xColumnName = MPUtil.normalizeColumnName(HomestayRentalEntity.class, xColumnName);
    yColumnName = MPUtil.normalizeColumnName(HomestayRentalEntity.class, yColumnName);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("xColumn", xColumnName);
    params.put("yColumn", yColumnName);
    QueryWrapper<HomestayRentalEntity> ew = scopedStatsWrapper(request);
    List<Map<String, Object>> result = homestayRentalService.selectValue(params, ew);
    MPUtil.aliasMapKey(result, originalXColumnName, xColumnName);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    for (Map<String, Object> m : result) {
      for (String k : m.keySet()) {
        if (m.get(k) instanceof Date) {
          m.put(k, sdf.format((Date) m.get(k)));
        }
      }
    }
    return R.ok().put("data", result);
  }

  /** （按值统计(多)） */
  @RequestMapping("/valueMul/{xColumnName}")
  public R valueMul(
      @PathVariable("xColumnName") String xColumnName,
      @RequestParam String yColumnNameMul,
      HttpServletRequest request) {
    R denied = requireLogin(request);
    if (denied != null) return denied;
    String[] yColumnNames = yColumnNameMul.split(",");
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("xColumn", xColumnName);
    List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String, Object>>>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    QueryWrapper<HomestayRentalEntity> ew = scopedStatsWrapper(request);
    for (int i = 0; i < yColumnNames.length; i++) {
      params.put("yColumn", yColumnNames[i]);
      List<Map<String, Object>> result = homestayRentalService.selectValue(params, ew);
      for (Map<String, Object> m : result) {
        for (String k : m.keySet()) {
          if (m.get(k) instanceof Date) {
            m.put(k, sdf.format((Date) m.get(k)));
          }
        }
      }
      result2.add(result);
    }
    return R.ok().put("data", result2);
  }

  /** （按值统计）时间统计类型 */
  @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
  public R valueDay(
      @PathVariable("yColumnName") String yColumnName,
      @PathVariable("xColumnName") String xColumnName,
      @PathVariable("timeStatType") String timeStatType,
      HttpServletRequest request) {
    R denied = requireLogin(request);
    if (denied != null) return denied;
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("xColumn", xColumnName);
    params.put("yColumn", yColumnName);
    params.put("timeStatType", timeStatType);
    QueryWrapper<HomestayRentalEntity> ew = scopedStatsWrapper(request);
    List<Map<String, Object>> result = homestayRentalService.selectTimeStatValue(params, ew);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    for (Map<String, Object> m : result) {
      for (String k : m.keySet()) {
        if (m.get(k) instanceof Date) {
          m.put(k, sdf.format((Date) m.get(k)));
        }
      }
    }
    return R.ok().put("data", result);
  }

  /** （按值统计）时间统计类型(多) */
  @RequestMapping("/valueMul/{xColumnName}/{timeStatType}")
  public R valueMulDay(
      @PathVariable("xColumnName") String xColumnName,
      @PathVariable("timeStatType") String timeStatType,
      @RequestParam String yColumnNameMul,
      HttpServletRequest request) {
    R denied = requireLogin(request);
    if (denied != null) return denied;
    String[] yColumnNames = yColumnNameMul.split(",");
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("xColumn", xColumnName);
    params.put("timeStatType", timeStatType);
    List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String, Object>>>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    QueryWrapper<HomestayRentalEntity> ew = scopedStatsWrapper(request);
    for (int i = 0; i < yColumnNames.length; i++) {
      params.put("yColumn", yColumnNames[i]);
      List<Map<String, Object>> result = homestayRentalService.selectTimeStatValue(params, ew);
      for (Map<String, Object> m : result) {
        for (String k : m.keySet()) {
          if (m.get(k) instanceof Date) {
            m.put(k, sdf.format((Date) m.get(k)));
          }
        }
      }
      result2.add(result);
    }
    return R.ok().put("data", result2);
  }

  /** 分组统计 */
  @RequestMapping("/group/{columnName}")
  public R group(@PathVariable("columnName") String columnName, HttpServletRequest request) {
    R denied = requireLogin(request);
    if (denied != null) return denied;
    String originalColumnName = columnName;
    columnName = MPUtil.normalizeColumnName(HomestayRentalEntity.class, columnName);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("column", columnName);
    QueryWrapper<HomestayRentalEntity> ew = scopedStatsWrapper(request);
    List<Map<String, Object>> result = homestayRentalService.selectGroup(params, ew);
    MPUtil.aliasMapKey(result, originalColumnName, columnName);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    for (Map<String, Object> m : result) {
      for (String k : m.keySet()) {
        if (m.get(k) instanceof Date) {
          m.put(k, sdf.format((Date) m.get(k)));
        }
      }
    }
    return R.ok().put("data", result);
  }

  private R fillOrderFromSource(HomestayRentalEntity order) {
    PlatformViewEntity homestay = merchantStayLookup.find(order.getSourceType(), order.getHomestayId());
    if (homestay == null || homestay.getPricePerDay() == null || homestay.getPricePerDay() < 0) {
      return R.error(404, "民宿不存在、未归属商家或价格无效");
    }
    order.setSourceType("platform_view");
    order.setHomestayId(homestay.getId());
    order.setHomestayName(homestay.getHomestayName());
    order.setHomestayImage(homestay.getHomestayImage());
    order.setHomestayLocation(homestay.getHomestayLocation());
    order.setHomestayCategory(homestay.getHomestayCategory());
    order.setPricePerDay(homestay.getPricePerDay());
    order.setMerchantAccount(homestay.getMerchantAccount());
    order.setMerchantName(homestay.getMerchantName());
    order.setMerchantPhone(homestay.getMerchantPhone());
    return R.ok();
  }

  private HomestayRentalEntity currentConsumerOrder(Long id, HttpServletRequest request) {
    String username = currentConsumerUsername(request);
    if (username == null || id == null) return null;
    return homestayRentalService.getOne(
        new QueryWrapper<HomestayRentalEntity>().eq("id", id).eq("account", username));
  }

  private Long currentUserId(HttpServletRequest request) {
    Object value = request.getSession().getAttribute("userId");
    if (value == null) return null;
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Long currentConsumerId(HttpServletRequest request) {
    return isConsumer(request) ? currentUserId(request) : null;
  }

  private String currentUsername(HttpServletRequest request) {
    Object value = request.getSession().getAttribute("username");
    return value == null || value.toString().trim().isEmpty() ? null : value.toString();
  }

  private String currentConsumerUsername(HttpServletRequest request) {
    return isConsumer(request) ? currentUsername(request) : null;
  }

  private boolean isConsumer(HttpServletRequest request) {
    return "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")));
  }

  private R consumerAuthError(HttpServletRequest request) {
    return currentUserId(request) == null ? R.error(401, "请先登录") : R.error(403, "仅消费者可操作订单");
  }

  private LocalDate toLocalDate(Date date) {
    return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
  }

  private boolean isSupportedSource(String sourceType) {
    return "homestay_info".equals(sourceType) || "platform_view".equals(sourceType);
  }

  /** 总数量 */
  @RequestMapping("/count")
  public R count(
      @RequestParam Map<String, Object> params,
      HomestayRentalEntity homestayRental,
      HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    applyOwnerScope(homestayRental, request);
    normalizeApprovalFilter(homestayRental);
    QueryWrapper<HomestayRentalEntity> ew = new QueryWrapper<HomestayRentalEntity>();
    long count =
        homestayRentalService.count(
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayRental), params), params));
    return R.ok().put("data", count);
  }

  private R requireLogin(HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    return null;
  }

  private QueryWrapper<HomestayRentalEntity> scopedStatsWrapper(HttpServletRequest request) {
    QueryWrapper<HomestayRentalEntity> ew = new QueryWrapper<HomestayRentalEntity>();
    if (AuthSupport.isMerchant(request)) {
      ew.eq("merchant_account", AuthSupport.username(request));
    } else if (AuthSupport.isConsumer(request)) {
      ew.eq("account", AuthSupport.username(request));
    }
    return ew;
  }

  private R readOrder(Long id, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    HomestayRentalEntity order = homestayRentalService.getById(id);
    if (order == null || !canAccessOrder(order, request)) {
      return R.error(404, "订单不存在");
    }
    attachRefundToOrder(order);
    resolveListing(order, true);
    if (AuthSupport.isAdmin(request) || AuthSupport.isMerchant(request)) {
      maskRentalRow(order);
    }
    return R.ok().put("data", order);
  }

  private void applyOwnerScope(HomestayRentalEntity order, HttpServletRequest request) {
    if (order == null) return;
    if (AuthSupport.isMerchant(request)) {
      order.setMerchantAccount(AuthSupport.username(request));
    } else if (AuthSupport.isConsumer(request)) {
      order.setAccount(AuthSupport.username(request));
    }
  }

  private void maskRentalPage(PageUtils page) {
    if (page == null || page.getList() == null) return;
    for (Object item : page.getList()) {
      if (item instanceof HomestayRentalEntity) {
        maskRentalRow((HomestayRentalEntity) item);
      }
    }
  }

  private void maskRentalRow(HomestayRentalEntity order) {
    if (order == null) return;
    order.setRealName(PrivacyMask.maskName(order.getRealName()));
    order.setPhone(PrivacyMask.maskPhone(order.getPhone()));
  }

  private void resolveListings(PageUtils page) {
    if (page == null || page.getList() == null) return;
    for (Object item : page.getList()) {
      if (item instanceof HomestayRentalEntity) {
        resolveListing((HomestayRentalEntity) item, true);
      }
    }
  }

  private void resolveListing(HomestayRentalEntity order, boolean persist) {
    if (order == null) return;
    PlatformViewEntity stay =
        merchantStayLookup.resolve(
            order.getSourceType(),
            order.getHomestayId(),
            order.getMerchantAccount(),
            order.getHomestayName());
    if (stay == null) return;
    Long stayId = stay.getId();
    boolean dirty =
        stayId != null
            && (!stayId.equals(order.getHomestayId()) || !"platform_view".equals(order.getSourceType()));
    order.setHomestayId(stayId);
    order.setSourceType("platform_view");
    if (persist && dirty && order.getId() != null) {
      HomestayRentalEntity patch = new HomestayRentalEntity();
      patch.setId(order.getId());
      patch.setHomestayId(stayId);
      patch.setSourceType("platform_view");
      homestayRentalService.updateById(patch);
    }
  }

  private void attachRefundStatus(PageUtils page) {
    if (page == null || page.getList() == null || page.getList().isEmpty()) return;
    List<Long> ids = new ArrayList<>();
    for (Object item : page.getList()) {
      if (item instanceof HomestayRentalEntity) {
        Long id = ((HomestayRentalEntity) item).getId();
        if (id != null) ids.add(id);
      }
    }
    if (ids.isEmpty()) return;
    List<RefundRequestEntity> refunds =
        refundRequestService.list(
            new QueryWrapper<RefundRequestEntity>().in("order_id", ids).orderByDesc("id"));
    Map<Long, RefundRequestEntity> latest = new HashMap<>();
    for (RefundRequestEntity refund : refunds) {
      if (refund.getOrderId() != null && !latest.containsKey(refund.getOrderId())) {
        latest.put(refund.getOrderId(), refund);
      }
    }
    for (Object item : page.getList()) {
      if (!(item instanceof HomestayRentalEntity)) continue;
      HomestayRentalEntity order = (HomestayRentalEntity) item;
      RefundRequestEntity refund = latest.get(order.getId());
      if (refund == null) continue;
      order.setRefundId(refund.getId());
      order.setRefundStatus(refund.getStatus());
      order.setRefundRejectCount(refund.getRejectCount());
      order.setRefundReason(refund.getReason());
      order.setRefundEvidence(refund.getEvidence());
      order.setRefundAppealReason(refund.getAppealReason());
    }
  }

  private void attachRefundToOrder(HomestayRentalEntity order) {
    if (order == null || order.getId() == null) return;
    RefundRequestEntity refund =
        refundRequestService.getOne(
            new QueryWrapper<RefundRequestEntity>()
                .eq("order_id", order.getId())
                .orderByDesc("id")
                .last("limit 1"),
            false);
    if (refund == null) return;
    order.setRefundId(refund.getId());
    order.setRefundStatus(refund.getStatus());
    order.setRefundRejectCount(refund.getRejectCount());
    order.setRefundReason(refund.getReason());
    order.setRefundEvidence(refund.getEvidence());
    order.setRefundAppealReason(refund.getAppealReason());
  }

  private boolean canAccessOrder(HomestayRentalEntity order, HttpServletRequest request) {
    if (order == null) return false;
    if (AuthSupport.isAdmin(request)) return true;
    String username = AuthSupport.username(request);
    if (AuthSupport.isMerchant(request)) {
      return StringUtils.equals(username, order.getMerchantAccount());
    }
    if (AuthSupport.isConsumer(request)) {
      return StringUtils.equals(username, order.getAccount());
    }
    return false;
  }

  private boolean isApproved(String approvalStatus) {
    return "是".equals(approvalStatus) || "已通过".equals(approvalStatus);
  }

  private static boolean isDecidedApproval(String approvalStatus) {
    return "是".equals(approvalStatus)
        || "已通过".equals(approvalStatus)
        || "否".equals(approvalStatus)
        || "已拒绝".equals(approvalStatus);
  }

  private void stampMerchantReviewedIfNeeded(HomestayRentalEntity order) {
    if (order == null || order.getId() == null) return;
    if (!isDecidedApproval(order.getApprovalStatus()) || order.getMerchantReviewedAt() != null) return;
    HomestayRentalEntity patch = new HomestayRentalEntity();
    patch.setId(order.getId());
    patch.setMerchantReviewedAt(new Date());
    homestayRentalService.updateById(patch);
  }

  private void normalizeApprovalFilter(HomestayRentalEntity order) {
    if (order == null || order.getApprovalStatus() == null) return;
    if ("是".equals(order.getApprovalStatus())) order.setApprovalStatus("已通过");
    else if ("否".equals(order.getApprovalStatus())) order.setApprovalStatus("已拒绝");
  }
}
