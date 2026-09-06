package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SupportTicketDao;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.entity.SupportTicketEntity;
import com.neststay.entity.SupportTicketMessageEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RefundRequestService;
import com.neststay.service.SupportTicketMessageService;
import com.neststay.service.SupportTicketService;
import com.neststay.service.SystemNoticeService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportTicketServiceImpl extends ServiceImpl<SupportTicketDao, SupportTicketEntity>
    implements SupportTicketService {
  public static final List<String> CATEGORIES =
      Arrays.asList("退款争议", "房源不符", "入住体验", "评价争议", "支付问题", "账号隐私", "其他");
  private static final List<String> OPEN =
      Arrays.asList("待商家", "待平台", "处理中");
  private static final List<String> PLATFORM_FIRST = Arrays.asList("支付问题", "账号隐私");

  @Autowired private SupportTicketMessageService messageService;
  @Autowired private HomestayRentalService rentalService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private ConsumerService consumerService;
  @Autowired private SystemNoticeService systemNoticeService;

  @Override
  public List<String> categories() {
    return CATEGORIES;
  }

  @Override
  public PageUtils pageTickets(Map<String, Object> params, HttpServletRequest request) {
    int page = intParam(params.get("page"), 1);
    int limit = intParam(params.get("limit"), 10);
    QueryWrapper<SupportTicketEntity> wrapper = new QueryWrapper<>();
    if (AuthSupport.isConsumer(request)) {
      wrapper.eq("consumer_id", AuthSupport.userId(request));
    } else if (AuthSupport.isMerchant(request)) {
      wrapper.eq("merchant_account", AuthSupport.username(request));
    } else if (!AuthSupport.isAdmin(request)) {
      return new PageUtils(new Page<>(page, limit));
    }
    String status = string(params.get("status"));
    if (StringUtils.isNotBlank(status)) wrapper.eq("status", status);
    String category = string(params.get("category"));
    if (StringUtils.isNotBlank(category)) wrapper.eq("category", category);
    String keyword = string(params.get("keyword"));
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(
          w ->
              w.like("ticket_no", keyword)
                  .or()
                  .like("title", keyword)
                  .or()
                  .like("order_number", keyword)
                  .or()
                  .like("consumer_account", keyword));
    }
    wrapper.orderByDesc("id");
    Page<SupportTicketEntity> result = page(new Page<>(page, limit), wrapper);
    for (SupportTicketEntity ticket : result.getRecords()) {
      decorate(ticket, request, false);
    }
    return new PageUtils(result);
  }

  @Override
  @Transactional
  public R create(SupportTicketEntity body, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(403, "请使用消费者账号提交申述");
    if (body == null || StringUtils.isBlank(body.getCategory()) || StringUtils.isBlank(body.getTitle())
        || StringUtils.isBlank(body.getContent())) {
      return R.error(400, "请填写类型、标题和描述");
    }
    if (!CATEGORIES.contains(body.getCategory().trim())) {
      return R.error(400, "不支持的申述类型");
    }
    String title = body.getTitle().trim();
    String content = body.getContent().trim();
    if (title.length() > 80) return R.error(400, "标题最多 80 字");
    if (content.length() > 2000) return R.error(400, "描述最多 2000 字");
    HomestayRentalEntity order = null;
    if (body.getOrderId() != null) {
      order = rentalService.getById(body.getOrderId());
      if (order == null || !StringUtils.equals(order.getAccount(), AuthSupport.username(request))) {
        return R.error(404, "未找到该订单");
      }
    }
    PlatformViewEntity listing = null;
    Long listingId = body.getListingId();
    if (listingId == null && order != null) listingId = order.getHomestayId();
    if (listingId != null) listing = platformViewService.getById(listingId);
    String category = body.getCategory().trim();
    QueryWrapper<SupportTicketEntity> dup = new QueryWrapper<SupportTicketEntity>()
        .eq("consumer_id", AuthSupport.userId(request))
        .eq("category", category)
        .in("status", OPEN);
    if (order != null) dup.eq("order_id", order.getId());
    else if (listingId != null) dup.eq("listing_id", listingId);
    else dup.isNull("order_id").isNull("listing_id");
    if (count(dup) > 0) {
      return R.error(409, "已有同类型进行中的工单，请在原工单继续沟通");
    }
    SupportTicketEntity ticket = new SupportTicketEntity();
    ticket.setCategory(category);
    ticket.setPriority(
        StringUtils.defaultIfBlank(
            body.getPriority(), "入住体验".equals(category) ? "紧急" : "普通"));
    if (!"紧急".equals(ticket.getPriority())) ticket.setPriority("普通");
    ticket.setTitle(title);
    ticket.setContent(content);
    ticket.setEvidence(StringUtils.trimToNull(body.getEvidence()));
    ticket.setConsumerId(AuthSupport.userId(request));
    ticket.setConsumerAccount(AuthSupport.username(request));
    ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
    ticket.setConsumerName(
        consumer == null
            ? AuthSupport.username(request)
            : StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
    if (order != null) {
      ticket.setOrderId(order.getId());
      ticket.setOrderNumber(order.getOrderNumber());
      ticket.setMerchantAccount(order.getMerchantAccount());
      ticket.setListingId(order.getHomestayId());
      ticket.setListingName(order.getHomestayName());
    }
    if (listing != null) {
      ticket.setListingId(listing.getId());
      ticket.setListingName(listing.getHomestayName());
      if (StringUtils.isBlank(ticket.getMerchantAccount())) {
        ticket.setMerchantAccount(listing.getMerchantAccount());
      }
    }
    RefundRequestEntity refund = latestRefund(ticket.getOrderId());
    if (refund != null && "退款争议".equals(category)) {
      ticket.setRefundId(refund.getId());
    }
    boolean platformFirst =
        PLATFORM_FIRST.contains(category) || StringUtils.isBlank(ticket.getMerchantAccount());
    if (platformFirst || (refund != null && "平台介入".equals(refund.getStatus()))) {
      ticket.setStatus("待平台");
    } else {
      ticket.setStatus("待商家");
      Calendar due = Calendar.getInstance();
      due.add(Calendar.HOUR_OF_DAY, 48);
      ticket.setMerchantDueAt(due.getTime());
    }
    Date now = new Date();
    ticket.setCreateTime(now);
    ticket.setUpdateTime(now);
    ticket.setLastReplierRole("consumer");
    ticket.setLastReplyAt(now);
    save(ticket);
    ticket.setTicketNo(String.format("NST%06d", ticket.getId()));
    updateById(ticket);
    addMessage(ticket, "consumer", AuthSupport.userId(request), ticket.getConsumerName(), content, ticket.getEvidence(), "create");
    systemNoticeService.ticketOpened(ticket.getConsumerId(), ticket.getTicketNo(), ticket.getId(), ticket.getTitle());
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  public R detail(Long id, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(id, request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public R reply(Map<String, Object> body, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(toLong(body.get("id")), request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, false);
    if (!Boolean.TRUE.equals(ticket.getCanReply())) return R.error(409, "当前状态不可回复");
    String content = string(body.get("content"));
    if (StringUtils.isBlank(content)) return R.error(400, "请填写回复内容");
    String role = role(request);
    addMessage(ticket, role, AuthSupport.userId(request), displayName(request), content.trim(), string(body.get("attachments")), "reply");
    if ("待商家".equals(ticket.getStatus()) && "merchant".equals(role)) ticket.setStatus("处理中");
    if ("待平台".equals(ticket.getStatus()) && "admin".equals(role)) ticket.setStatus("处理中");
    touch(ticket, role);
    updateById(ticket);
    notifyConsumer(ticket, "工单有新回复", "工单 " + ticket.getTicketNo() + " 收到新回复");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public R escalate(Map<String, Object> body, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(toLong(body.get("id")), request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, false);
    if (!Boolean.TRUE.equals(ticket.getCanEscalate())) return R.error(409, "当前不可转交平台");
    String reason = string(body.get("reason"));
    if (StringUtils.isBlank(reason)) return R.error(400, "请说明转交平台的原因");
    String role = role(request);
    ticket.setStatus("待平台");
    addMessage(
        ticket,
        role,
        AuthSupport.userId(request),
        displayName(request),
        reason.trim(),
        null,
        "escalate");
    touch(ticket, role);
    updateById(ticket);
    notifyConsumer(ticket, "已转交平台", "工单 " + ticket.getTicketNo() + " 已申请平台介入");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public R resolve(Map<String, Object> body, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(toLong(body.get("id")), request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, false);
    if (!Boolean.TRUE.equals(ticket.getCanResolve())) return R.error(409, "当前不可标记解决");
    String note = StringUtils.defaultIfBlank(string(body.get("reason")), "已处理完毕");
    String role = role(request);
    ticket.setStatus("已解决");
    addMessage(ticket, role, AuthSupport.userId(request), displayName(request), note, null, "resolve");
    touch(ticket, role);
    updateById(ticket);
    notifyConsumer(ticket, "工单已解决", "工单 " + ticket.getTicketNo() + " 已标记为解决，如仍有问题可重新打开");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public R close(Map<String, Object> body, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(toLong(body.get("id")), request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, false);
    if (!Boolean.TRUE.equals(ticket.getCanClose())) return R.error(409, "当前不可关闭");
    String reason = StringUtils.defaultIfBlank(string(body.get("reason")), "已关闭");
    String role = role(request);
    ticket.setStatus("已关闭");
    ticket.setClosedAt(new Date());
    ticket.setCloseReason(reason);
    addMessage(ticket, role, AuthSupport.userId(request), displayName(request), reason, null, "close");
    touch(ticket, role);
    updateById(ticket);
    notifyConsumer(ticket, "工单已关闭", "工单 " + ticket.getTicketNo() + " 已关闭");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public R reopen(Map<String, Object> body, HttpServletRequest request) {
    SupportTicketEntity ticket = loadOwned(toLong(body.get("id")), request);
    if (ticket == null) return R.error(404, "工单不存在或无权查看");
    decorate(ticket, request, false);
    if (!Boolean.TRUE.equals(ticket.getCanReopen())) return R.error(409, "当前不可重新打开");
    String reason = StringUtils.defaultIfBlank(string(body.get("reason")), "问题仍未解决，申请重新处理");
    ticket.setStatus("待平台");
    ticket.setClosedAt(null);
    ticket.setCloseReason(null);
    addMessage(ticket, "consumer", AuthSupport.userId(request), displayName(request), reason, null, "reopen");
    touch(ticket, "consumer");
    updateById(ticket);
    notifyConsumer(ticket, "工单已重开", "工单 " + ticket.getTicketNo() + " 已重新打开，等待平台处理");
    decorate(ticket, request, true);
    return R.ok().put("data", ticket);
  }

  @Override
  @Transactional
  public SupportTicketEntity ensureFromRefund(RefundRequestEntity refund) {
    if (refund == null) return null;
    SupportTicketEntity existing =
        getOne(
            new QueryWrapper<SupportTicketEntity>().eq("refund_id", refund.getId()).last("limit 1"),
            false);
    String appeal =
        StringUtils.defaultIfBlank(refund.getAppealReason(), refund.getReason());
    if (existing != null) {
      existing.setStatus("待平台");
      existing.setRefundId(refund.getId());
      addMessage(existing, "system", null, "系统", "退款已申请平台介入：" + StringUtils.defaultString(appeal), refund.getEvidence(), "escalate");
      touch(existing, "consumer");
      updateById(existing);
      return existing;
    }
    SupportTicketEntity ticket = new SupportTicketEntity();
    ticket.setCategory("退款争议");
    ticket.setPriority("紧急");
    ticket.setTitle("订单" + StringUtils.defaultString(refund.getOrderNumber()) + "退款争议");
    ticket.setContent(StringUtils.defaultIfBlank(appeal, "消费者申请平台介入退款"));
    ticket.setEvidence(refund.getEvidence());
    ticket.setStatus("待平台");
    ticket.setOrderId(refund.getOrderId());
    ticket.setOrderNumber(refund.getOrderNumber());
    ticket.setMerchantAccount(refund.getMerchantAccount());
    ticket.setConsumerAccount(refund.getConsumerAccount());
    ticket.setRefundId(refund.getId());
    if (refund.getOrderId() != null) {
      HomestayRentalEntity order = rentalService.getById(refund.getOrderId());
      if (order != null) {
        ticket.setListingId(order.getHomestayId());
        ticket.setListingName(order.getHomestayName());
      }
    }
    ConsumerEntity consumer =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", refund.getConsumerAccount()).last("limit 1"),
            false);
    if (consumer != null) {
      ticket.setConsumerId(consumer.getId());
      ticket.setConsumerName(StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
    }
    Date now = new Date();
    ticket.setCreateTime(now);
    ticket.setUpdateTime(now);
    ticket.setLastReplierRole("consumer");
    ticket.setLastReplyAt(now);
    save(ticket);
    ticket.setTicketNo(String.format("NST%06d", ticket.getId()));
    updateById(ticket);
    addMessage(ticket, "consumer", ticket.getConsumerId(), ticket.getConsumerName(), ticket.getContent(), ticket.getEvidence(), "create");
    if (ticket.getConsumerId() != null) {
      systemNoticeService.ticketOpened(ticket.getConsumerId(), ticket.getTicketNo(), ticket.getId(), ticket.getTitle());
    }
    return ticket;
  }

  private SupportTicketEntity loadOwned(Long id, HttpServletRequest request) {
    if (id == null) return null;
    SupportTicketEntity ticket = getById(id);
    if (ticket == null) return null;
    if (AuthSupport.isAdmin(request)) return ticket;
    if (AuthSupport.isConsumer(request) && AuthSupport.userId(request).equals(ticket.getConsumerId())) {
      return ticket;
    }
    if (AuthSupport.isMerchant(request)
        && StringUtils.equals(AuthSupport.username(request), ticket.getMerchantAccount())) {
      return ticket;
    }
    return null;
  }

  private void decorate(SupportTicketEntity ticket, HttpServletRequest request, boolean withMessages) {
    boolean overdue =
        ticket.getMerchantDueAt() != null
            && "待商家".equals(ticket.getStatus())
            && ticket.getMerchantDueAt().before(new Date());
    ticket.setOverdue(overdue);
    String role = role(request);
    boolean open = OPEN.contains(ticket.getStatus());
    ticket.setCanReply(open && ("consumer".equals(role) || "merchant".equals(role) || "admin".equals(role)));
    ticket.setCanEscalate(
        open
            && !"待平台".equals(ticket.getStatus())
            && ("consumer".equals(role) || "merchant".equals(role) || overdue && "admin".equals(role)));
    ticket.setCanResolve(open && ("merchant".equals(role) || "admin".equals(role)));
    ticket.setCanClose(
        ("admin".equals(role) && !"已关闭".equals(ticket.getStatus()))
            || ("consumer".equals(role) && "已解决".equals(ticket.getStatus())));
    ticket.setCanReopen("consumer".equals(role) && "已解决".equals(ticket.getStatus()));
    if (withMessages) {
      ticket.setMessages(
          messageService.list(
              new QueryWrapper<SupportTicketMessageEntity>()
                  .eq("ticket_id", ticket.getId())
                  .orderByAsc("id")));
    }
  }

  private void addMessage(
      SupportTicketEntity ticket,
      String role,
      Long senderId,
      String senderName,
      String content,
      String attachments,
      String action) {
    SupportTicketMessageEntity msg = new SupportTicketMessageEntity();
    msg.setTicketId(ticket.getId());
    msg.setSenderRole(role);
    msg.setSenderId(senderId);
    msg.setSenderName(senderName);
    msg.setContent(content);
    msg.setAttachments(StringUtils.trimToNull(attachments));
    msg.setAction(StringUtils.defaultIfBlank(action, "reply"));
    msg.setCreateTime(new Date());
    messageService.save(msg);
  }

  private void touch(SupportTicketEntity ticket, String role) {
    ticket.setLastReplierRole(role);
    ticket.setLastReplyAt(new Date());
    ticket.setUpdateTime(new Date());
  }

  private void notifyConsumer(SupportTicketEntity ticket, String title, String content) {
    if (ticket.getConsumerId() == null) return;
    systemNoticeService.ticketUpdate(ticket.getConsumerId(), title, content, ticket.getId());
  }

  private RefundRequestEntity latestRefund(Long orderId) {
    if (orderId == null) return null;
    return refundRequestService.getOne(
        new QueryWrapper<RefundRequestEntity>().eq("order_id", orderId).orderByDesc("id").last("limit 1"),
        false);
  }

  private String role(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return "admin";
    if (AuthSupport.isMerchant(request)) return "merchant";
    return "consumer";
  }

  private String displayName(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return "平台客服";
    if (AuthSupport.isMerchant(request)) return AuthSupport.username(request);
    ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
    if (consumer == null) return AuthSupport.username(request);
    return StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount());
  }

  private static String string(Object value) {
    if (value == null) return "";
    String text = String.valueOf(value);
    return "null".equals(text) ? "" : text;
  }

  private static Long toLong(Object value) {
    if (value == null || StringUtils.isBlank(String.valueOf(value))) return null;
    try {
      return Long.valueOf(String.valueOf(value));
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private static int intParam(Object value, int fallback) {
    if (value == null) return fallback;
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException exception) {
      return fallback;
    }
  }
}
