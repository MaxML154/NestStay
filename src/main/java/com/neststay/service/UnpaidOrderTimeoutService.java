package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayRentalEntity;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnpaidOrderTimeoutService {
  public static final int PAY_WINDOW_HOURS = 2;

  @Autowired private HomestayRentalService homestayRentalService;
  @Autowired private ConsumerService consumerService;
  @Autowired private SystemNoticeService systemNoticeService;

  public Date startPayWindow(HomestayRentalEntity order) {
    if (order == null || "已支付".equals(order.getIsPaid()) || "已退款".equals(order.getIsPaid())) return null;
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR_OF_DAY, PAY_WINDOW_HOURS);
    Date deadline = calendar.getTime();
    order.setPayDeadline(deadline);
    notifyConsumer(
        order,
        () ->
            systemNoticeService.payWindowOpened(
                consumerId(order),
                order.getOrderNumber(),
                order.getHomestayName(),
                order.getId()));
    return deadline;
  }

  public boolean isExpiredUnpaid(HomestayRentalEntity order) {
    if (order == null) return false;
    if ("已支付".equals(order.getIsPaid())) return false;
    if ("已退款".equals(order.getIsPaid())) return false;
    if (Arrays.asList("已取消", "已拒绝", "已入住", "已完成")
        .contains(StringUtils.defaultString(order.getOrderStatus()))) {
      return false;
    }
    Date deadline = order.getPayDeadline();
    return deadline != null && !deadline.after(new Date());
  }

  @Transactional
  public boolean expireIfDue(HomestayRentalEntity order) {
    if (!isExpiredUnpaid(order)) return false;
    cancelUnpaid(order, true);
    return true;
  }

  @Transactional
  public int expireDueOrders() {
    Date now = new Date();
    List<HomestayRentalEntity> due =
        homestayRentalService.list(
            new QueryWrapper<HomestayRentalEntity>()
                .and(w -> w.ne("is_paid", "已支付").or().isNull("is_paid"))
                .in("approval_status", Arrays.asList("已通过", "是"))
                .and(
                    w ->
                        w.notIn(
                                "order_status",
                                Arrays.asList("已取消", "已拒绝", "已入住", "已完成"))
                            .or()
                            .isNull("order_status"))
                .isNotNull("pay_deadline")
                .le("pay_deadline", now));
    int n = 0;
    for (HomestayRentalEntity order : due) {
      cancelUnpaid(order, true);
      n++;
    }
    return n;
  }

  private void cancelUnpaid(HomestayRentalEntity order, boolean notify) {
    order.setOrderStatus("已取消");
    order.setApprovalStatus("已取消");
    homestayRentalService.updateById(order);
    if (!notify) return;
    notifyConsumer(
        order,
        () ->
            systemNoticeService.payTimeoutCancelled(
                consumerId(order),
                order.getOrderNumber(),
                order.getHomestayName(),
                order.getId()));
  }

  private void notifyConsumer(HomestayRentalEntity order, Runnable push) {
    if (consumerId(order) == null) return;
    push.run();
  }

  private Long consumerId(HomestayRentalEntity order) {
    if (order == null || StringUtils.isBlank(order.getAccount())) return null;
    ConsumerEntity consumer =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", order.getAccount()).last("limit 1"),
            false);
    return consumer == null ? null : consumer.getId();
  }
}
