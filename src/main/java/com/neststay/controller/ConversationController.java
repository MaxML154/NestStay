package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.ConversationEntity;
import com.neststay.entity.ConversationMessageEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.ConversationMessageService;
import com.neststay.service.ConversationService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.MerchantService;
import com.neststay.service.MerchantStayLookup;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RefundRequestService;
import com.neststay.service.SupportTicketService;
import com.neststay.service.SystemNoticeService;
import com.neststay.service.XiaoBoService;
import com.neststay.service.MerchantAssistantService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversation")
public class ConversationController {
  @Autowired private ConversationService conversationService;
  @Autowired private ConversationMessageService messageService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private MerchantStayLookup merchantStayLookup;
  @Autowired private HomestayRentalService rentalService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private SupportTicketService supportTicketService;
  @Autowired private SystemNoticeService systemNoticeService;
  @Autowired private XiaoBoService xiaoBoService;
  @Autowired private MerchantAssistantService merchantAssistantService;
  @Autowired private ConsumerService consumerService;
  @Autowired private MerchantService merchantService;

  @RequestMapping("/page")
  public R page(@RequestParam Map<String, Object> params, HttpServletRequest request) {
    QueryWrapper<ConversationEntity> wrapper = scoped(request);
    if (wrapper == null) return R.error(401, "请先登录");
    int page = parseInt(params.get("page"), 1);
    int limit = parseInt(params.get("limit"), 10);
    Page<ConversationEntity> result =
        conversationService.page(new Page<>(page, limit), wrapper.orderByDesc("last_time"));
    fillMerchantProfiles(result.getRecords());
    fillConsumerProfiles(result.getRecords());
    return R.ok().put("data", new PageUtils(result));
  }

  @RequestMapping("/list")
  public R list(@RequestParam(required = false) String convType, HttpServletRequest request) {
    QueryWrapper<ConversationEntity> wrapper = scoped(request);
    if (wrapper == null) return R.error(401, "请先登录");
    if (StringUtils.isNotBlank(convType)) {
      wrapper.eq("conv_type", convType);
    } else if (AuthSupport.isConsumer(request)) {
      wrapper.eq("conv_type", "merchant");
    }
    List<ConversationEntity> rows =
        conversationService.list(wrapper.orderByDesc("last_time"));
    fillMerchantProfiles(rows);
    fillConsumerProfiles(rows);
    return R.ok().put("data", rows);
  }

  @RequestMapping("/open")
  @Transactional
  public R open(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(403, "请使用消费者账号联系商家或客服");
    String type = String.valueOf(body.getOrDefault("convType", "merchant"));
    if ("system".equals(type)) {
      return R.error(400, "系统通知请前往消息页「系统通知」查看");
    }
    ConversationEntity existing;
    if ("platform".equals(type)) {
      if (!xiaoBoService.canTransfer(request)) {
        return R.error(403, xiaoBoService.transferDeniedMessage(request));
      }
      return R.ok().put("data", openPlatform(request));
    }
    Long homestayId = toLong(body.get("homestayId"));
    String sourceType = String.valueOf(body.getOrDefault("sourceType", "platform_view"));
    if (homestayId == null) return R.error(400, "请选择要咨询的民宿");
    fillHomestay(body, homestayId, sourceType);
    if (StringUtils.isBlank(String.valueOf(body.get("merchantAccount")))
        || "platform".equals(body.get("merchantAccount"))) {
      return R.error(400, "该民宿未归属商家，无法发起咨询");
    }
    existing =
        conversationService.getOne(
            new QueryWrapper<ConversationEntity>()
                .eq("consumer_id", AuthSupport.userId(request))
                .eq("merchant_account", String.valueOf(body.get("merchantAccount")))
                .eq("conv_type", "merchant")
                .last("limit 1"),
            false);
    if (existing == null) {
      existing = new ConversationEntity();
      existing.setConvType("merchant");
      existing.setConsumerId(AuthSupport.userId(request));
      existing.setConsumerAccount(AuthSupport.username(request));
      existing.setMerchantAccount(String.valueOf(body.get("merchantAccount")));
      existing.setDeletedByConsumer(0);
      existing.setDeletedByPeer(0);
    }
    existing.setHomestayId(toLong(body.get("homestayId")));
    existing.setSourceType("platform_view");
    existing.setHomestayName((String) body.get("homestayName"));
    existing.setHomestayImage((String) body.get("homestayImage"));
    existing.setPricePerDay(toInt(body.get("pricePerDay")));
    existing.setLastTime(new Date());
    conversationService.saveOrUpdate(existing);
    return R.ok().put("data", existing);
  }

  @RequestMapping("/switchHomestay/{id}")
  public R switchHomestay(
      @PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
    ConversationEntity conv = loadOwned(id, request);
    if (conv == null) return R.error(404, "会话不存在");
    Long homestayId = toLong(body.get("homestayId"));
    String sourceType = String.valueOf(body.getOrDefault("sourceType", conv.getSourceType()));
    fillHomestay(body, homestayId, sourceType);
    if (!StringUtils.equals(conv.getMerchantAccount(), String.valueOf(body.get("merchantAccount")))) {
      return R.error(403, "只能切换同一商家的其他民宿");
    }
    conv.setHomestayId(toLong(body.get("homestayId")));
    conv.setSourceType("platform_view");
    conv.setHomestayName((String) body.get("homestayName"));
    conv.setHomestayImage((String) body.get("homestayImage"));
    conv.setPricePerDay(toInt(body.get("pricePerDay")));
    conversationService.updateById(conv);
    fillMerchantProfiles(conv);
    fillConsumerProfiles(conv);
    return R.ok().put("data", conv);
  }

  @RequestMapping("/messages/{id}")
  public R messages(@PathVariable Long id, HttpServletRequest request) {
    ConversationEntity conv = loadOwned(id, request);
    if (conv == null) return R.error(404, "会话不存在");
    markPeerMessagesRead(id, request);
    List<ConversationMessageEntity> list =
        messageService.list(
            new QueryWrapper<ConversationMessageEntity>()
                .eq("conversation_id", id)
                .orderByAsc("id"));
    fillMerchantProfiles(conv);
    fillConsumerProfiles(conv);
    Map<String, Object> data = new HashMap<>();
    data.put("conversation", conv);
    data.put("list", list);
    return R.ok().put("data", data);
  }

  @RequestMapping("/inbox")
  public R inbox(@RequestParam(required = false) Long afterId, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    List<ConversationEntity> convs =
        conversationService.list(
            new QueryWrapper<ConversationEntity>()
                .eq("consumer_id", AuthSupport.userId(request))
                .in("conv_type", "merchant", "platform")
                .eq("deleted_by_consumer", 0)
                .select("id", "conv_type", "homestay_name", "homestay_image", "merchant_account"));
    if (convs.isEmpty()) return R.ok().put("data", Collections.emptyList());
    Map<Long, ConversationEntity> convMap =
        convs.stream().collect(Collectors.toMap(ConversationEntity::getId, item -> item, (a, b) -> a));
    QueryWrapper<ConversationMessageEntity> wrapper =
        new QueryWrapper<ConversationMessageEntity>()
            .in("conversation_id", convMap.keySet())
            .in("sender_role", "merchant", "admin", "assistant");
    if (afterId != null && afterId > 0) {
      wrapper.gt("id", afterId).orderByAsc("id").last("limit 30");
    } else {
      wrapper.orderByDesc("id").last("limit 1");
    }
    List<Map<String, Object>> rows = new ArrayList<>();
    for (ConversationMessageEntity message : messageService.list(wrapper)) {
      ConversationEntity conv = convMap.get(message.getConversationId());
      if (conv == null) continue;
      Map<String, Object> row = new HashMap<>();
      row.put("id", message.getId());
      row.put("conversationId", message.getConversationId());
      row.put("convType", conv.getConvType());
      row.put(
          "title",
          "platform".equals(conv.getConvType())
              ? "平台客服"
              : StringUtils.defaultIfBlank(
                  merchantDisplayName(conv.getMerchantAccount()),
                  StringUtils.defaultIfBlank(conv.getHomestayName(), "商家消息")));
      row.put("content", message.getContent());
      row.put("senderRole", message.getSenderRole());
      row.put("createTime", message.getCreateTime());
      rows.add(row);
    }
    return R.ok().put("data", rows);
  }

  @RequestMapping("/assistant/status")
  public R assistantStatus(HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    return R.ok().put("data", xiaoBoService.status(request));
  }

  @RequestMapping("/assistant/ask")
  @Transactional
  public R assistantAsk(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    String question = body == null ? "" : String.valueOf(body.getOrDefault("content", ""));
    if (StringUtils.isBlank(question) || "null".equals(question)) {
      return R.error(400, "请先输入要咨询的问题");
    }
    Map<String, Object> data = xiaoBoService.ask(question, request);
    if (Boolean.TRUE.equals(data.get("transfer"))) {
      ConversationEntity conv = openPlatform(request);
      ConversationMessageEntity message = new ConversationMessageEntity();
      message.setConversationId(conv.getId());
      message.setSenderId(AuthSupport.userId(request));
      message.setSenderRole("consumer");
      message.setContent(question);
      message.setMessageType("text");
      messageService.save(message);
      conv.setLastMessage(previewLastMessage(message));
      conv.setLastTime(new Date());
      conversationService.updateById(conv);
      data.put("conversation", conv);
    }
    return R.ok().put("data", data);
  }

  @RequestMapping("/assistant/transfer")
  @Transactional
  public R assistantTransfer(HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    if (!xiaoBoService.canTransfer(request)) {
      return R.error(403, xiaoBoService.transferDeniedMessage(request));
    }
    return R.ok().put("data", openPlatform(request));
  }

  @RequestMapping("/send")
  @Transactional
  public R send(@RequestBody ConversationMessageEntity message, HttpServletRequest request) {
    if (message == null || message.getConversationId() == null) {
      return R.error(400, "消息不能为空");
    }
    String type = StringUtils.defaultIfBlank(message.getMessageType(), "text");
    if (!"image".equals(type) && !"video".equals(type) && StringUtils.isBlank(message.getContent())) {
      return R.error(400, "消息不能为空");
    }
    if (("image".equals(type) || "video".equals(type)) && StringUtils.isBlank(message.getContent())) {
      return R.error(400, "请先上传文件");
    }
    ConversationEntity conv = loadOwned(message.getConversationId(), request);
    if (conv == null) return R.error(404, "会话不存在");
    if (AuthSupport.isAdmin(request) && !"platform".equals(conv.getConvType())
        && !"system".equals(conv.getConvType())) {
      long refunds =
          refundRequestService.count(
              new QueryWrapper<RefundRequestEntity>()
                  .eq("conversation_id", conv.getId())
                  .eq("status", "平台介入"));
      if (refunds == 0) {
        return R.error(403, "仅在用户发起客服会话或退款介入后，管理员才能联系消费者");
      }
    }
    message.setId(null);
    message.setSenderId(AuthSupport.userId(request));
    message.setSenderRole(senderRole(request));
    message.setMessageType(type);
    messageService.save(message);
    conv.setLastMessage(previewLastMessage(message));
    conv.setLastTime(new Date());
    conversationService.updateById(conv);
    if (AuthSupport.isConsumer(request)
        && "merchant".equals(conv.getConvType())
        && "text".equals(type)) {
      try {
        merchantAssistantService.maybeReply(conv, message.getContent());
      } catch (Exception ignored) {
        // 模型失败不阻断用户消息，也不自动转人工
      }
    }
    return R.ok().put("data", message);
  }

  @RequestMapping("/delete/{id}")
  public R delete(@PathVariable Long id, HttpServletRequest request) {
    ConversationEntity conv = loadOwned(id, request);
    if (conv == null) return R.error(404, "会话不存在");
    if (AuthSupport.isConsumer(request)) conv.setDeletedByConsumer(1);
    else conv.setDeletedByPeer(1);
    conversationService.updateById(conv);
    return R.ok();
  }

  @RequestMapping("/merchantHomestays")
  public R merchantHomestays(@RequestParam String merchantAccount) {
    List<PlatformViewEntity> list =
        platformViewService.list(
            new QueryWrapper<PlatformViewEntity>()
                .eq("merchant_account", merchantAccount)
                .and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status")));
    return R.ok().put("data", list);
  }

  @RequestMapping("/refund/apply")
  @Transactional
  public R applyRefund(@RequestBody RefundRequestEntity refund, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(403, "仅消费者可申请退款协商");
    if (refund == null || refund.getOrderId() == null) {
      return R.error(400, "请从订单页发起退款");
    }
    HomestayRentalEntity order = rentalService.getById(refund.getOrderId());
    if (order == null || !AuthSupport.username(request).equals(order.getAccount())) {
      return R.error(404, "未找到可协商的订单，请从订单页发起");
    }
    if (!"已支付".equals(order.getIsPaid())) {
      return R.error(409, "未支付订单无需退款");
    }
    if (Arrays.asList("已取消", "已拒绝", "已完成").contains(StringUtils.defaultString(order.getOrderStatus()))) {
      return R.error(409, "当前订单状态不可申请退款");
    }
    if (StringUtils.isBlank(refund.getReason())) {
      return R.error(400, "请填写退款原因");
    }
    RefundRequestEntity latest = latestRefund(order.getId());
    if (latest != null) {
      String status = latest.getStatus();
      if ("协商中".equals(status) || "平台介入".equals(status)) {
        return R.error(409, "该订单已有进行中的退款申请");
      }
      if ("商家驳回".equals(status)) {
        return R.error(409, "商家已驳回，请在订单页申请平台介入");
      }
      if ("商家同意".equals(status) || "已退款".equals(status)) {
        return R.error(409, "该订单已完成退款");
      }
    }
    String reason = refund.getReason().trim();
    ConversationEntity conv = ensureOrderConversation(order, request);
    String notice =
        "您的订单"
            + StringUtils.defaultString(order.getOrderNumber())
            + "因「"
            + reason
            + "」申请退款，请与商家核实后，待商家同意后即可退款";
    refund.setId(null);
    refund.setOrderId(order.getId());
    refund.setConversationId(conv.getId());
    refund.setConsumerAccount(order.getAccount());
    refund.setMerchantAccount(order.getMerchantAccount());
    refund.setOrderNumber(order.getOrderNumber());
    refund.setReason(reason);
    refund.setStatus("协商中");
    refund.setRejectCount(0);
    refundRequestService.save(refund);
    postNoticeMessage(conv, notice);
    systemNoticeService.refundAccepted(
        consumerIdByAccount(order.getAccount()),
        order.getOrderNumber(),
        order.getId(),
        reason);
    return R.ok().put("data", refund);
  }

  @RequestMapping("/refund/decide")
  @Transactional
  public R decideRefund(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    RefundRequestEntity refund = refundRequestService.getById(toLong(body.get("id")));
    if (refund == null && body.get("conversationId") != null) {
      QueryWrapper<RefundRequestEntity> wrapper =
          new QueryWrapper<RefundRequestEntity>()
              .eq("conversation_id", toLong(body.get("conversationId")))
              .orderByDesc("id")
              .last("limit 1");
      if (AuthSupport.isConsumer(request)) {
        wrapper.eq("consumer_account", AuthSupport.username(request));
      }
      refund = refundRequestService.getOne(wrapper, false);
    }
    if (refund == null && body.get("orderId") != null) {
      QueryWrapper<RefundRequestEntity> wrapper =
          new QueryWrapper<RefundRequestEntity>()
              .eq("order_id", toLong(body.get("orderId")))
              .orderByDesc("id")
              .last("limit 1");
      if (AuthSupport.isMerchant(request)) {
        wrapper.eq("merchant_account", AuthSupport.username(request));
      }
      refund = refundRequestService.getOne(wrapper, false);
    }
    if (refund == null) return R.error(404, "退款申请不存在");
    String action = String.valueOf(body.get("action"));
    if (AuthSupport.isMerchant(request)
        && AuthSupport.username(request).equals(refund.getMerchantAccount())) {
      if (!"协商中".equals(refund.getStatus())) {
        return R.error(409, "当前退款状态不可由商家处理");
      }
      if ("agree".equals(action)) {
        refund.setStatus("商家同意");
        refundRequestService.updateById(refund);
        settleRefundedOrder(refund.getOrderId());
        systemNoticeService.refundPassed(
            consumerIdByAccount(refund.getConsumerAccount()),
            refund.getOrderNumber(),
            refund.getOrderId());
        postRefundNotice(
            refund, "商家已同意订单" + StringUtils.defaultString(refund.getOrderNumber()) + "的退款申请");
      } else {
        refund.setRejectCount(refund.getRejectCount() == null ? 1 : refund.getRejectCount() + 1);
        refund.setStatus("商家驳回");
        refundRequestService.updateById(refund);
        String reason = String.valueOf(body.getOrDefault("reason", ""));
        if ("null".equals(reason)) reason = "";
        systemNoticeService.refundRejected(
            consumerIdByAccount(refund.getConsumerAccount()),
            refund.getOrderNumber(),
            refund.getOrderId(),
            StringUtils.defaultIfBlank(reason, refund.getReason()));
        postRefundNotice(
            refund,
            "商家已驳回订单"
                + StringUtils.defaultString(refund.getOrderNumber())
                + "的退款申请"
                + (StringUtils.isBlank(reason) ? "" : "：" + reason.trim()));
      }
      return R.ok().put("data", refund);
    }
    if (AuthSupport.isConsumer(request) && "escalate".equals(action)) {
      if (!"商家驳回".equals(refund.getStatus())
          || refund.getRejectCount() == null
          || refund.getRejectCount() < 1) {
        return R.error(403, "商家驳回后方可在订单页申请平台介入");
      }
      String appeal = String.valueOf(body.getOrDefault("reason", ""));
      if ("null".equals(appeal)) appeal = "";
      String evidence = String.valueOf(body.getOrDefault("evidence", ""));
      if ("null".equals(evidence)) evidence = "";
      if (StringUtils.isBlank(appeal)) {
        return R.error(400, "请填写申请平台介入的原因");
      }
      refund.setAppealReason(appeal.trim());
      if (StringUtils.isNotBlank(evidence)) refund.setEvidence(evidence.trim());
      refund.setStatus("平台介入");
      refundRequestService.updateById(refund);
      postRefundNotice(
          refund,
          "消费者已申请平台介入，补充原因：" + appeal.trim());
      systemNoticeService.refundEscalated(
          consumerIdByAccount(refund.getConsumerAccount()),
          refund.getOrderNumber(),
          refund.getOrderId());
      supportTicketService.ensureFromRefund(refund);
      return R.ok().put("data", refund);
    }
    if (AuthSupport.isAdmin(request) && "平台介入".equals(refund.getStatus())) {
      boolean agree = "agree".equals(action);
      refund.setStatus(agree ? "已退款" : "平台驳回");
      refundRequestService.updateById(refund);
      Long uid = consumerIdByAccount(refund.getConsumerAccount());
      if (agree) {
        settleRefundedOrder(refund.getOrderId());
        systemNoticeService.refundPassed(uid, refund.getOrderNumber(), refund.getOrderId());
        postRefundNotice(refund, "平台已同意订单" + StringUtils.defaultString(refund.getOrderNumber()) + "的退款申请");
      } else {
        String reason = String.valueOf(body.getOrDefault("reason", ""));
        if ("null".equals(reason)) reason = "";
        systemNoticeService.refundRejected(
            uid,
            refund.getOrderNumber(),
            refund.getOrderId(),
            StringUtils.defaultIfBlank(reason, "平台未通过"));
        postRefundNotice(
            refund,
            "平台未通过订单"
                + StringUtils.defaultString(refund.getOrderNumber())
                + "的退款申请"
                + (StringUtils.isBlank(reason) ? "" : "：" + reason.trim()));
      }
      return R.ok().put("data", refund);
    }
    return R.error(403, "无权处理该退款申请");
  }

  private QueryWrapper<ConversationEntity> scoped(HttpServletRequest request) {
    QueryWrapper<ConversationEntity> wrapper = new QueryWrapper<>();
    if (AuthSupport.isConsumer(request)) {
      wrapper.eq("consumer_id", AuthSupport.userId(request)).eq("deleted_by_consumer", 0);
      return wrapper;
    }
    if (AuthSupport.isMerchant(request)) {
      wrapper
          .eq("deleted_by_peer", 0)
          .and(
              w ->
                  w.eq("merchant_account", AuthSupport.username(request))
                      .or(
                          x ->
                              x.eq("conv_type", "system")
                                  .eq("consumer_id", AuthSupport.userId(request))));
      return wrapper;
    }
    if (AuthSupport.isAdmin(request)) {
      QueryWrapper<ConversationEntity> adminWrapper = new QueryWrapper<>();
      adminWrapper.and(
          w ->
              w.eq("conv_type", "platform")
                  .or()
                  .exists(
                      "select 1 from refund_request r where r.conversation_id = conversation.id and r.status = '平台介入'"));
      return adminWrapper;
    }
    return null;
  }

  private ConversationEntity openPlatform(HttpServletRequest request) {
    ConversationEntity existing =
        conversationService.getOne(
            new QueryWrapper<ConversationEntity>()
                .eq("consumer_id", AuthSupport.userId(request))
                .eq("conv_type", "platform")
                .last("limit 1"),
            false);
    if (existing == null) {
      existing = new ConversationEntity();
      existing.setConvType("platform");
      existing.setConsumerId(AuthSupport.userId(request));
      existing.setConsumerAccount(AuthSupport.username(request));
      existing.setHomestayName("平台客服");
      existing.setLastTime(new Date());
      existing.setDeletedByConsumer(0);
      existing.setDeletedByPeer(0);
      conversationService.save(existing);
    } else if (existing.getDeletedByConsumer() != null && existing.getDeletedByConsumer() == 1) {
      existing.setDeletedByConsumer(0);
      conversationService.updateById(existing);
    }
    return existing;
  }

  private Long consumerIdByAccount(String account) {
    if (StringUtils.isBlank(account)) return null;
    ConsumerEntity consumer =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", account).last("limit 1"), false);
    return consumer == null ? null : consumer.getId();
  }

  private ConversationEntity loadOwned(Long id, HttpServletRequest request) {
    ConversationEntity conv = conversationService.getById(id);
    if (conv == null) return null;
    if (AuthSupport.isConsumer(request) && AuthSupport.userId(request).equals(conv.getConsumerId())) {
      return conv;
    }
    if (AuthSupport.isMerchant(request)
        && StringUtils.equals(AuthSupport.username(request), conv.getMerchantAccount())) {
      return conv;
    }
    if (AuthSupport.isAdmin(request)) return conv;
    return null;
  }

  private void fillHomestay(Map<String, Object> body, Long homestayId, String sourceType) {
    PlatformViewEntity homestay = merchantStayLookup.find(sourceType, homestayId);
    if (homestay == null) return;
    body.put("homestayName", homestay.getHomestayName());
    body.put("homestayImage", homestay.getHomestayImage());
    body.put("pricePerDay", homestay.getPricePerDay());
    body.put("merchantAccount", homestay.getMerchantAccount());
    body.put("sourceType", "platform_view");
    body.put("homestayId", homestay.getId());
  }

  private String senderRole(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return "admin";
    if (AuthSupport.isMerchant(request)) return "merchant";
    return "consumer";
  }

  private void fillMerchantProfiles(ConversationEntity conv) {
    if (conv == null) return;
    fillMerchantProfiles(Collections.singletonList(conv));
  }

  private void fillConsumerProfiles(ConversationEntity conv) {
    if (conv == null) return;
    fillConsumerProfiles(Collections.singletonList(conv));
  }

  private void fillConsumerProfiles(List<ConversationEntity> rows) {
    if (rows == null || rows.isEmpty()) return;
    Set<Long> ids = new HashSet<>();
    Set<String> accounts = new HashSet<>();
    for (ConversationEntity conv : rows) {
      if (conv == null) continue;
      if (conv.getConsumerId() != null) ids.add(conv.getConsumerId());
      if (StringUtils.isNotBlank(conv.getConsumerAccount())) accounts.add(conv.getConsumerAccount());
    }
    if (ids.isEmpty() && accounts.isEmpty()) return;
    QueryWrapper<ConsumerEntity> wrapper = new QueryWrapper<>();
    wrapper.and(
        w -> {
          if (!ids.isEmpty()) w.in("id", ids);
          if (!ids.isEmpty() && !accounts.isEmpty()) w.or();
          if (!accounts.isEmpty()) w.in("account", accounts);
        });
    List<ConsumerEntity> consumers = consumerService.list(wrapper);
    Map<Long, ConsumerEntity> byId = new HashMap<>();
    Map<String, ConsumerEntity> byAccount = new HashMap<>();
    for (ConsumerEntity consumer : consumers) {
      if (consumer == null) continue;
      if (consumer.getId() != null) byId.put(consumer.getId(), consumer);
      if (StringUtils.isNotBlank(consumer.getAccount())) byAccount.put(consumer.getAccount(), consumer);
    }
    for (ConversationEntity conv : rows) {
      if (conv == null) continue;
      ConsumerEntity consumer = conv.getConsumerId() != null ? byId.get(conv.getConsumerId()) : null;
      if (consumer == null && StringUtils.isNotBlank(conv.getConsumerAccount())) {
        consumer = byAccount.get(conv.getConsumerAccount());
      }
      if (consumer == null) {
        conv.setConsumerNickname(
            StringUtils.defaultIfBlank(conv.getConsumerAccount(), "消费者"));
        continue;
      }
      conv.setConsumerNickname(
          StringUtils.defaultIfBlank(
              consumer.getNickname(),
              StringUtils.defaultIfBlank(consumer.getAccount(), "消费者")));
      conv.setConsumerAvatar(consumer.getAvatar());
    }
  }

  private void fillMerchantProfiles(List<ConversationEntity> rows) {
    if (rows == null || rows.isEmpty()) return;
    Set<String> accounts = new HashSet<>();
    for (ConversationEntity conv : rows) {
      if (conv != null && StringUtils.isNotBlank(conv.getMerchantAccount())) {
        accounts.add(conv.getMerchantAccount());
      }
    }
    if (accounts.isEmpty()) return;
    List<MerchantEntity> merchants =
        merchantService.list(
            new QueryWrapper<MerchantEntity>().in("merchant_account", accounts));
    Map<String, MerchantEntity> byAccount = new HashMap<>();
    for (MerchantEntity merchant : merchants) {
      if (merchant != null && StringUtils.isNotBlank(merchant.getMerchantAccount())) {
        byAccount.put(merchant.getMerchantAccount(), merchant);
      }
    }
    for (ConversationEntity conv : rows) {
      if (conv == null) continue;
      MerchantEntity merchant = byAccount.get(conv.getMerchantAccount());
      if (merchant == null) {
        conv.setMerchantName(conv.getMerchantAccount());
        continue;
      }
      conv.setMerchantName(
          StringUtils.defaultIfBlank(merchant.getMerchantName(), merchant.getMerchantAccount()));
      conv.setMerchantAvatar(merchant.getAvatar());
    }
  }

  private String merchantDisplayName(String account) {
    if (StringUtils.isBlank(account)) return "";
    MerchantEntity merchant =
        merchantService.getOne(
            new QueryWrapper<MerchantEntity>().eq("merchant_account", account).last("limit 1"),
            false);
    if (merchant == null) return account;
    return StringUtils.defaultIfBlank(merchant.getMerchantName(), account);
  }

  private void markPeerMessagesRead(Long conversationId, HttpServletRequest request) {
    if (conversationId == null) return;
    String myRole = senderRole(request);
    messageService.update(
        new UpdateWrapper<ConversationMessageEntity>()
            .eq("conversation_id", conversationId)
            .ne("sender_role", myRole)
            .isNull("read_at")
            .set("read_at", new Date()));
  }

  private String previewLastMessage(ConversationMessageEntity message) {
    String type = StringUtils.defaultIfBlank(message.getMessageType(), "text");
    if ("image".equals(type)) return "[图片]";
    if ("video".equals(type)) return "[视频]";
    return StringUtils.abbreviate(message.getContent(), 80);
  }

  private void postRefundNotice(RefundRequestEntity refund, String content) {
    if (refund == null || refund.getConversationId() == null) return;
    postNoticeMessage(conversationService.getById(refund.getConversationId()), content);
  }

  private ConversationEntity ensureOrderConversation(
      HomestayRentalEntity order, HttpServletRequest request) {
    ConversationEntity existing =
        conversationService.getOne(
            new QueryWrapper<ConversationEntity>()
                .eq("consumer_id", AuthSupport.userId(request))
                .eq("merchant_account", order.getMerchantAccount())
                .eq("conv_type", "merchant")
                .last("limit 1"),
            false);
    if (existing == null) {
      existing = new ConversationEntity();
      existing.setConvType("merchant");
      existing.setConsumerId(AuthSupport.userId(request));
      existing.setConsumerAccount(order.getAccount());
      existing.setMerchantAccount(order.getMerchantAccount());
      existing.setDeletedByConsumer(0);
      existing.setDeletedByPeer(0);
    }
    existing.setDeletedByConsumer(0);
    existing.setDeletedByPeer(0);
    existing.setHomestayId(order.getHomestayId());
    existing.setSourceType(StringUtils.defaultIfBlank(order.getSourceType(), "platform_view"));
    existing.setHomestayName(order.getHomestayName());
    existing.setHomestayImage(order.getHomestayImage());
    existing.setPricePerDay(order.getPricePerDay());
    existing.setLastTime(new Date());
    conversationService.saveOrUpdate(existing);
    return existing;
  }

  private void postNoticeMessage(ConversationEntity conv, String content) {
    if (conv == null || conv.getId() == null || StringUtils.isBlank(content)) return;
    ConversationMessageEntity message = new ConversationMessageEntity();
    message.setConversationId(conv.getId());
    message.setSenderRole("system");
    message.setSenderId(0L);
    message.setContent(content);
    message.setMessageType("notice");
    messageService.save(message);
    conv.setLastMessage(StringUtils.abbreviate(content, 80));
    conv.setLastTime(new Date());
    conversationService.updateById(conv);
  }

  private void settleRefundedOrder(Long orderId) {
    if (orderId == null) return;
    HomestayRentalEntity order = rentalService.getById(orderId);
    if (order == null) return;
    order.setOrderStatus("已取消");
    if ("已支付".equals(order.getIsPaid())) {
      order.setIsPaid("已退款");
    }
    rentalService.updateById(order);
  }

  private RefundRequestEntity latestRefund(Long orderId) {
    if (orderId == null) return null;
    return refundRequestService.getOne(
        new QueryWrapper<RefundRequestEntity>().eq("order_id", orderId).orderByDesc("id").last("limit 1"),
        false);
  }

  private int parseInt(Object value, int fallback) {
    try {
      return value == null ? fallback : Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private Long toLong(Object value) {
    try {
      return value == null ? null : Long.valueOf(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Integer toInt(Object value) {
    try {
      return value == null ? null : Integer.valueOf(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
