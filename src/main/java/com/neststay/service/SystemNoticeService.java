package com.neststay.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SystemNoticeDao;
import com.neststay.entity.SystemNoticeEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/** 系统通知写入 system_notice 卡片，禁止再写入 conv_type=system 会话气泡。 */
@Service
public class SystemNoticeService extends ServiceImpl<SystemNoticeDao, SystemNoticeEntity> {
  public static final String ORDER_REFUND = "order_refund";
  public static final String STAY = "stay";
  public static final String AUDIT = "audit";
  public static final String ACCOUNT = "account";
  public static final String ANNOUNCE = "announce";
  public static final String TICKET = "ticket";
  public static final String ORDER_PAY = "order_pay";

  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public void notifyUser(Long userId, String account, String content) {
    if (userId == null || StringUtils.isBlank(content)) return;
    push(userId, ANNOUNCE, "系统通知", content, null, null, "/index/messages?tab=notice");
  }

  public void refundAccepted(Long userId, String orderNumber, Long orderId, String reason) {
    push(
        userId,
        ORDER_REFUND,
        "退款申请",
        "您的订单"
            + safe(orderNumber)
            + "因「"
            + StringUtils.defaultIfBlank(reason, "未说明")
            + "」申请退款，请与商家核实后，待商家同意后即可退款",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void refundEscalated(Long userId, String orderNumber, Long orderId) {
    push(
        userId,
        ORDER_REFUND,
        "平台介入",
        "您的订单"
            + safe(orderNumber)
            + "已申请平台介入，平台将审核该退款申请",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void refundPassed(Long userId, String orderNumber, Long orderId) {
    String ts = nowTs();
    push(
        userId,
        ORDER_REFUND,
        "退款通过",
        "您的订单"
            + safe(orderNumber)
            + "的退款申请已经于"
            + ts
            + "通过，退款将会在3个自然日内返回到原支付方式的账户中",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void refundRejected(Long userId, String orderNumber, Long orderId, String reason) {
    String ts = nowTs();
    push(
        userId,
        ORDER_REFUND,
        "退款拒绝",
        "您的订单"
            + safe(orderNumber)
            + "的退款申请已经于"
            + ts
            + "被拒绝，原因："
            + StringUtils.defaultIfBlank(reason, "未说明原因"),
        orderId,
        "order",
        orderJump(orderId));
  }

  public void stayConfirmed(Long userId, String homestayName, Long orderId) {
    String ts = nowTs();
    push(
        userId,
        STAY,
        "入住确认",
        "商家已于" + ts + "确认您入住" + safe(homestayName),
        orderId,
        "order",
        orderJump(orderId));
  }

  public void stayCheckedOut(Long userId, String homestayName, Long orderId) {
    String ts = nowTs();
    push(
        userId,
        STAY,
        "签退完成",
        "您已于" + ts + "完成" + safe(homestayName) + "的签退，欢迎评价本次入住",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void forumAudit(Long userId, String title, boolean passed, String reason, Long postId) {
    String ts = nowTs();
    String content =
        passed
            ? "您的帖子《" + safe(title) + "》已于" + ts + "通过审核"
            : "您的帖子《"
                + safe(title)
                + "》已于"
                + ts
                + "未通过，原因："
                + StringUtils.defaultIfBlank(reason, "不符合社区规范");
    push(
        userId,
        AUDIT,
        passed ? "帖子审核通过" : "帖子审核未通过",
        content,
        postId,
        "forum",
        postId == null ? "/index/forum" : "/index/forumDetail?id=" + postId);
  }

  public void forumHold(Long userId, String title, String reason, Long postId) {
    push(
        userId,
        AUDIT,
        "帖子待人工复核",
        "您的帖子《"
            + safe(title)
            + "》已提交，"
            + StringUtils.defaultIfBlank(reason, "需管理员复核后才会出现在社区论坛"),
        postId,
        "forum",
        postId == null ? "/index/forum" : "/index/forumDetail?id=" + postId);
  }

  public void newsAudit(Long userId, String title, String status, String reason, Long newsId) {
    boolean passed = "已通过".equals(status);
    boolean hold = "待审核".equals(status);
    String headline = passed ? "资讯已发布" : (hold ? "资讯待人工复核" : "资讯未通过");
    String body =
        hold
            ? "您的资讯《"
                + safe(title)
                + "》需管理员复核。"
                + StringUtils.defaultIfBlank(reason, "")
            : passed
                ? "您的资讯《" + safe(title) + "》已发布"
                : "您的资讯《"
                    + safe(title)
                    + "》未通过，原因："
                    + StringUtils.defaultIfBlank(reason, "不符合发布规范");
    push(
        userId,
        AUDIT,
        headline,
        body,
        newsId,
        "news",
        newsId == null ? "/index/news" : "/index/news-detail?id=" + newsId);
  }

  public void accountNotice(Long userId, String title, String content, String jumpPath) {
    push(userId, ACCOUNT, StringUtils.defaultIfBlank(title, "账户通知"), content, null, "account", jumpPath);
  }

  public void payWindowOpened(Long userId, String orderNumber, String homestayName, Long orderId) {
    push(
        userId,
        ORDER_PAY,
        "请在2小时内支付",
        "您预订的「"
            + safe(homestayName)
            + "」（订单"
            + safe(orderNumber)
            + "）已审核通过，请在2小时内完成支付，超时将自动取消预订",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void payTimeoutCancelled(Long userId, String orderNumber, String homestayName, Long orderId) {
    push(
        userId,
        ORDER_PAY,
        "预订已取消",
        "您预订的「"
            + safe(homestayName)
            + "」（订单"
            + safe(orderNumber)
            + "）因超过2小时未支付，已自动取消",
        orderId,
        "order",
        orderJump(orderId));
  }

  public void ticketOpened(Long userId, String ticketNo, Long ticketId, String title) {
    push(
        userId,
        TICKET,
        "申述已提交",
        "工单 "
            + safe(ticketNo)
            + "「"
            + safe(title)
            + "」已提交，商家将在 48 小时内处理，超时可转交平台",
        ticketId,
        "ticket",
        ticketJump(ticketId));
  }

  public void ticketUpdate(Long userId, String title, String content, Long ticketId) {
    push(userId, TICKET, StringUtils.defaultIfBlank(title, "工单更新"), content, ticketId, "ticket", ticketJump(ticketId));
  }

  public void push(
      Long userId,
      String category,
      String title,
      String content,
      Long bizId,
      String bizType,
      String jumpPath) {
    if (userId == null || StringUtils.isBlank(content)) return;
    SystemNoticeEntity notice = new SystemNoticeEntity();
    notice.setUserId(userId);
    notice.setCategory(StringUtils.defaultIfBlank(category, ANNOUNCE));
    notice.setTitle(StringUtils.defaultIfBlank(title, "系统通知"));
    notice.setContent(content);
    notice.setBizId(bizId);
    notice.setBizType(bizType);
    notice.setJumpPath(StringUtils.defaultIfBlank(jumpPath, "/index/messages?tab=notice"));
    notice.setReadFlag(0);
    save(notice);
  }

  private String ticketJump(Long ticketId) {
    return ticketId == null ? "/index/tickets" : "/index/tickets?id=" + ticketId;
  }

  private String orderJump(Long orderId) {
    return orderId == null
        ? "/index/center?tab=orders"
        : "/index/homestayRentalDetail?id=" + orderId + "&centerType=1";
  }

  private String nowTs() {
    return LocalDateTime.now().format(TS);
  }

  private String safe(String value) {
    return StringUtils.defaultString(value);
  }
}
