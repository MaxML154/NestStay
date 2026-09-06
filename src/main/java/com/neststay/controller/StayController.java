package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.entity.StayServiceEntity;
import com.neststay.entity.StayServiceRequestEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.RefundRequestService;
import com.neststay.service.StayServiceCatalogService;
import com.neststay.service.StayServiceRequestService;
import com.neststay.service.SystemNoticeService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PageUtils;
import com.neststay.utils.PrivacyMask;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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

@RestController
@RequestMapping({"/stay", "/stayService"})
public class StayController {
  @Autowired private HomestayRentalService rentalService;
  @Autowired private StayServiceCatalogService catalogService;
  @Autowired private StayServiceRequestService requestService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private SystemNoticeService systemNoticeService;
  @Autowired private ConsumerService consumerService;

  @RequestMapping("/current")
  public R current(HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    List<HomestayRentalEntity> list =
        rentalService.list(
            new QueryWrapper<HomestayRentalEntity>()
                .eq("account", AuthSupport.username(request))
                .in("order_status", "待入住", "已入住")
                .orderByDesc("id"));
    for (HomestayRentalEntity order : list) {
      attachRefund(order);
    }
    return R.ok().put("data", list);
  }

  /** 商家入住看板：按房源分组，单元是民宿名+订单，无独立房间号。平台管理员只读。 */
  @RequestMapping("/occupancy")
  public R occupancy(HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    QueryWrapper<HomestayRentalEntity> wrapper = new QueryWrapper<>();
    wrapper.in("order_status", "待入住", "已入住");
    if (AuthSupport.isMerchant(request)) {
      wrapper.eq("merchant_account", AuthSupport.username(request));
    }
    wrapper.orderByAsc("homestay_name").orderByAsc("check_in_date").orderByAsc("id");
    List<HomestayRentalEntity> orders = rentalService.list(wrapper);
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
    Map<String, Map<String, Object>> groups = new LinkedHashMap<>();
    for (HomestayRentalEntity order : orders) {
      String name = StringUtils.defaultIfBlank(order.getHomestayName(), "未命名房源");
      Map<String, Object> group = groups.get(name);
      if (group == null) {
        group = new LinkedHashMap<>();
        group.put("homestayName", name);
        group.put("homestayImage", order.getHomestayImage());
        group.put("stays", new ArrayList<Map<String, Object>>());
        groups.put(name, group);
      }
      Map<String, Object> stay = new LinkedHashMap<>();
      stay.put("orderId", order.getId());
      stay.put("orderNumber", order.getOrderNumber());
      stay.put("account", order.getAccount());
      stay.put("maskedRealName", PrivacyMask.maskName(order.getRealName()));
      stay.put("checkInDate", formatYmd(order.getCheckInDate()));
      stay.put("checkOutDate", formatYmd(order.getCheckOutDate()));
      stay.put("orderStatus", order.getOrderStatus());
      stay.put("occupancyStatus", occupancyStatus(order, today));
      stay.put("homestayId", order.getHomestayId());
      stay.put("sourceType", order.getSourceType());
      RefundRequestEntity refund = latestRefund(order.getId());
      if (refund != null) stay.put("refundStatus", refund.getStatus());
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> stays = (List<Map<String, Object>>) group.get("stays");
      stays.add(stay);
    }
    return R.ok().put("data", new ArrayList<>(groups.values()));
  }

  private String occupancyStatus(HomestayRentalEntity order, LocalDate today) {
    if ("待入住".equals(order.getOrderStatus())) return "待入住";
    if ("已入住".equals(order.getOrderStatus()) && order.getCheckOutDate() != null) {
      LocalDate out = toLocalDate(order.getCheckOutDate());
      if (out != null && !out.isAfter(today)) return "待签退";
    }
    return "在住";
  }

  private LocalDate toLocalDate(Date date) {
    if (date == null) return null;
    if (date instanceof java.sql.Date) return ((java.sql.Date) date).toLocalDate();
    return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
  }

  private String formatYmd(Date date) {
    LocalDate local = toLocalDate(date);
    return local == null ? null : local.toString();
  }

  private RefundRequestEntity latestRefund(Long orderId) {
    if (orderId == null) return null;
    return refundRequestService.getOne(
        new QueryWrapper<RefundRequestEntity>()
            .eq("order_id", orderId)
            .orderByDesc("id")
            .last("limit 1"),
        false);
  }

  private boolean hasBlockingRefund(Long orderId) {
    RefundRequestEntity refund = latestRefund(orderId);
    if (refund == null) return false;
    return "协商中".equals(refund.getStatus()) || "平台介入".equals(refund.getStatus());
  }

  private void attachRefund(HomestayRentalEntity order) {
    RefundRequestEntity refund = latestRefund(order.getId());
    if (refund == null) return;
    order.setRefundStatus(refund.getStatus());
    order.setRefundId(refund.getId());
    order.setRefundRejectCount(refund.getRejectCount());
  }

  @RequestMapping("/catalog")
  public R catalog(HttpServletRequest request, String merchantAccount) {
    if (AuthSupport.isMerchant(request)) merchantAccount = AuthSupport.username(request);
    if (StringUtils.isBlank(merchantAccount)) return R.error(400, "缺少商家账号");
    return R.ok()
        .put(
            "data",
            catalogService.list(
                new QueryWrapper<StayServiceEntity>()
                    .eq("merchant_account", merchantAccount)
                    .eq("enabled", 1)));
  }

  @RequestMapping("/catalog/page")
  public R catalogPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    QueryWrapper<StayServiceEntity> wrapper = new QueryWrapper<>();
    if (AuthSupport.isMerchant(request)) {
      wrapper.eq("merchant_account", AuthSupport.username(request));
    }
    Page<StayServiceEntity> result =
        catalogService.page(new Page<>(page, limit), wrapper.orderByDesc("id"));
    return R.ok().put("data", new PageUtils(result));
  }

  @RequestMapping("/inbox")
  public R inbox(HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    QueryWrapper<StayServiceRequestEntity> wrapper = new QueryWrapper<>();
    if (AuthSupport.isMerchant(request)) {
      java.util.List<HomestayRentalEntity> orders =
          rentalService.list(
              new QueryWrapper<HomestayRentalEntity>()
                  .eq("merchant_account", AuthSupport.username(request)));
      if (orders.isEmpty()) return R.ok().put("data", java.util.Collections.emptyList());
      java.util.Map<Long, String> numbers = new java.util.HashMap<>();
      for (HomestayRentalEntity order : orders) {
        numbers.put(order.getId(), order.getOrderNumber());
      }
      wrapper.in("order_id", numbers.keySet());
      java.util.List<StayServiceRequestEntity> rows = requestService.list(wrapper.orderByDesc("id"));
      java.util.List<java.util.Map<String, Object>> data = new java.util.ArrayList<>();
      for (StayServiceRequestEntity row : rows) {
        java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("orderId", row.getOrderId());
        item.put("orderNumber", numbers.get(row.getOrderId()));
        item.put("serviceName", row.getServiceName());
        item.put("account", row.getAccount());
        item.put("status", row.getStatus());
        item.put("content", row.getContent());
        data.add(item);
      }
      return R.ok().put("data", data);
    }
    return R.ok().put("data", requestService.list(wrapper.orderByDesc("id")));
  }

  @RequestMapping("/request/handle")
  public R handleRequest(@RequestBody StayServiceRequestEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    StayServiceRequestEntity existing = requestService.getById(entity.getId());
    if (existing == null) return R.error(404, "服务请求不存在");
    existing.setStatus(StringUtils.defaultIfBlank(entity.getStatus(), "已完成"));
    requestService.updateById(existing);
    return R.ok();
  }

  @RequestMapping("/catalog/save")
  public R saveCatalog(@RequestBody StayServiceEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    if (entity == null || StringUtils.isBlank(entity.getServiceName())) {
      return R.error(400, "服务名称不能为空");
    }
    if (AuthSupport.isMerchant(request)) {
      entity.setMerchantAccount(AuthSupport.username(request));
    }
    if (entity.getEnabled() == null) entity.setEnabled(1);
    catalogService.saveOrUpdate(entity);
    return R.ok();
  }

  @RequestMapping("/catalog/delete")
  public R deleteCatalog(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isMerchant(request) && !AuthSupport.isAdmin(request)) {
      return AuthSupport.staffError(request);
    }
    catalogService.removeByIds(java.util.Arrays.asList(ids));
    return R.ok();
  }

  @RequestMapping("/request")
  public R createRequest(@RequestBody StayServiceRequestEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(403, "仅入住用户可呼叫服务");
    HomestayRentalEntity order = rentalService.getById(entity.getOrderId());
    if (order == null
        || !AuthSupport.username(request).equals(order.getAccount())
        || !"已入住".equals(order.getOrderStatus())) {
      return R.error(403, "仅当前入住订单可使用客房服务");
    }
    entity.setId(null);
    entity.setAccount(order.getAccount());
    entity.setStatus("待处理");
    if (entity.getServiceId() != null) {
      StayServiceEntity service = catalogService.getById(entity.getServiceId());
      if (service != null) entity.setServiceName(service.getServiceName());
    }
    if (StringUtils.isBlank(entity.getServiceName())) entity.setServiceName("客房服务");
    requestService.save(entity);
    return R.ok("已提交").put("data", entity);
  }

  @RequestMapping("/requests/{orderId}")
  public R requests(@PathVariable Long orderId, HttpServletRequest request) {
    HomestayRentalEntity order = rentalService.getById(orderId);
    if (order == null) return R.error(404, "订单不存在");
    if (AuthSupport.isConsumer(request) && !AuthSupport.username(request).equals(order.getAccount())) {
      return R.error(403, "无权查看");
    }
    if (AuthSupport.isMerchant(request)
        && !AuthSupport.username(request).equals(order.getMerchantAccount())) {
      return R.error(403, "无权查看");
    }
    return R.ok()
        .put(
            "data",
            requestService.list(
                new QueryWrapper<StayServiceRequestEntity>()
                    .eq("order_id", orderId)
                    .orderByDesc("id")));
  }

  @RequestMapping("/checkin/{id}")
  @Transactional
  public R checkin(@PathVariable Long id, HttpServletRequest request) {
    HomestayRentalEntity order = rentalService.getById(id);
    if (order == null) return R.error(404, "订单不存在");
    boolean ownerConsumer =
        AuthSupport.isConsumer(request) && AuthSupport.username(request).equals(order.getAccount());
    boolean ownerMerchant =
        AuthSupport.isMerchant(request)
            && AuthSupport.username(request).equals(order.getMerchantAccount());
    boolean staff = AuthSupport.isAdmin(request);
    if (!ownerConsumer && !ownerMerchant && !staff) {
      return R.error(403, "仅所属商家或入住用户可确认入住");
    }
    if (!"待入住".equals(order.getOrderStatus())) return R.error(409, "当前订单状态不可入住");
    if (hasBlockingRefund(order.getId())) {
      return R.error(409, "退款协商中，暂不能确认入住");
    }
    order.setOrderStatus("已入住");
    Date now = new Date();
    order.setCheckInAt(now);
    if (ownerMerchant && order.getMerchantCheckinAt() == null) {
      order.setMerchantCheckinAt(now);
    }
    rentalService.updateById(order);
    ConsumerEntity consumer =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", order.getAccount()).last("limit 1"),
            false);
    if (consumer != null) {
      systemNoticeService.stayConfirmed(consumer.getId(), order.getHomestayName(), order.getId());
    }
    return R.ok().put("data", order);
  }

  @RequestMapping("/checkout/{id}")
  @Transactional
  public R checkout(@PathVariable Long id, HttpServletRequest request) {
    HomestayRentalEntity order = rentalService.getById(id);
    if (order == null) return R.error(404, "订单不存在");
    boolean ownerConsumer =
        AuthSupport.isConsumer(request) && AuthSupport.username(request).equals(order.getAccount());
    boolean ownerMerchant =
        AuthSupport.isMerchant(request)
            && AuthSupport.username(request).equals(order.getMerchantAccount());
    if (!ownerConsumer && !ownerMerchant) {
      return R.error(403, "仅所属商家或入住用户可办理签退");
    }
    if (!"已入住".equals(order.getOrderStatus())) return R.error(409, "当前订单尚未入住");
    if (hasBlockingRefund(order.getId())) {
      return R.error(409, "退款处理中，暂不能签退");
    }
    order.setOrderStatus("已完成");
    Date now = new Date();
    order.setCheckOutAt(now);
    if (ownerMerchant && order.getMerchantCheckoutAt() == null) {
      order.setMerchantCheckoutAt(now);
    }
    rentalService.updateById(order);
    ConsumerEntity consumer =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", order.getAccount()).last("limit 1"),
            false);
    if (consumer != null) {
      systemNoticeService.stayCheckedOut(consumer.getId(), order.getHomestayName(), order.getId());
    }
    return R.ok().put("data", order);
  }
}
