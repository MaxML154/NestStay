package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.neststay.entity.ConversationEntity;
import com.neststay.entity.ConversationMessageEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.service.ConversationMessageService;
import com.neststay.service.ConversationService;
import com.neststay.service.HomestayIndexService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RefundRequestService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomestayIndexServiceImpl implements HomestayIndexService {
  public static final double SUGGEST_MIN_INDEX = 70;
  public static final int SUGGEST_MIN_REVIEWS = 3;

  private static final double W_RATING = 0.45;
  private static final double W_VOLUME = 0.15;
  private static final double W_FULFILL = 0.20;
  private static final double W_RESPONSE = 0.10;
  private static final double W_HEAT = 0.10;
  private static final double VOLUME_REF = 20;
  private static final double CLICK_REF = 200;
  private static final double FAV_REF = 30;
  private static final double ORDER_REF = 20;
  private static final double MISSING = 0.55;

  @Autowired private PlatformViewService platformViewService;
  @Autowired private HomestayRentalService rentalService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private ConversationService conversationService;
  @Autowired private ConversationMessageService conversationMessageService;

  @Override
  public void recalcListing(Long listingId) {
    if (listingId == null) return;
    PlatformViewEntity listing = platformViewService.getById(listingId);
    if (listing == null) return;
    IndexContext ctx = loadContext(java.util.Collections.singletonList(listing));
    persist(listing, compute(listing, ctx));
  }

  @Override
  public int recalcAll() {
    List<PlatformViewEntity> listings = platformViewService.list();
    if (listings.isEmpty()) return 0;
    IndexContext ctx = loadContext(listings);
    int n = 0;
    for (PlatformViewEntity listing : listings) {
      persist(listing, compute(listing, ctx));
      n++;
    }
    return n;
  }

  public static boolean suggested(PlatformViewEntity listing) {
    if (listing == null || listing.getPlatformIndex() == null) return false;
    int reviews = listing.getReviewCount() == null ? 0 : listing.getReviewCount();
    boolean passed =
        listing.getAuditStatus() == null || "已通过".equals(listing.getAuditStatus());
    return passed
        && listing.getPlatformIndex() >= SUGGEST_MIN_INDEX
        && reviews >= SUGGEST_MIN_REVIEWS;
  }

  private void persist(PlatformViewEntity listing, Score score) {
    platformViewService.update(
        new UpdateWrapper<PlatformViewEntity>()
            .eq("id", listing.getId())
            .set("platform_index", score.total)
            .set("idx_rating", score.rating)
            .set("idx_volume", score.volume)
            .set("idx_fulfill", score.fulfill)
            .set("idx_response", score.response)
            .set("idx_heat", score.heat));
    listing.setPlatformIndex(score.total);
    listing.setIdxRating(score.rating);
    listing.setIdxVolume(score.volume);
    listing.setIdxFulfill(score.fulfill);
    listing.setIdxResponse(score.response);
    listing.setIdxHeat(score.heat);
    listing.setSuggestedSelected(suggested(listing));
    if (listing.getSelectLock() == null || listing.getSelectLock() != 1) {
      int selected = suggested(listing) ? 1 : 0;
      platformViewService.update(
          new UpdateWrapper<PlatformViewEntity>()
              .eq("id", listing.getId())
              .set("is_selected", selected));
      listing.setIsSelected(selected);
    }
  }

  private Score compute(PlatformViewEntity listing, IndexContext ctx) {
    int reviews = listing.getReviewCount() == null ? 0 : listing.getReviewCount();
    double mean =
        listing.getAvgScore() == null
            ? HomestayRatingServiceImpl.PRIOR_MEAN
            : listing.getAvgScore();
    double smoothed =
        (reviews * mean + HomestayRatingServiceImpl.PRIOR_STRENGTH * HomestayRatingServiceImpl.PRIOR_MEAN)
            / (reviews + HomestayRatingServiceImpl.PRIOR_STRENGTH);
    double rating = clamp(smoothed / 5.0);
    double volume = clamp(Math.log(1 + reviews) / Math.log(1 + VOLUME_REF));
    double fulfill = fulfillScore(ctx.orders.getOrDefault(listing.getId(), java.util.Collections.emptyList()), ctx.refundByOrder);
    double response = responseScore(ctx.convs.getOrDefault(listing.getId(), java.util.Collections.emptyList()), ctx.messages);
    int clicks = listing.getClickCount() == null ? 0 : listing.getClickCount();
    int favs = listing.getFavoriteCount() == null ? 0 : listing.getFavoriteCount();
    int deals = 0;
    for (HomestayRentalEntity order : ctx.orders.getOrDefault(listing.getId(), java.util.Collections.emptyList())) {
      if (paid(order)) deals++;
    }
    double heat =
        clamp(
            0.4 * logNorm(clicks, CLICK_REF)
                + 0.3 * logNorm(favs, FAV_REF)
                + 0.3 * logNorm(deals, ORDER_REF));
    Score score = new Score();
    score.rating = pct(rating);
    score.volume = pct(volume);
    score.fulfill = pct(fulfill);
    score.response = pct(response);
    score.heat = pct(heat);
    score.total =
        round1(
            100
                * (W_RATING * rating
                    + W_VOLUME * volume
                    + W_FULFILL * fulfill
                    + W_RESPONSE * response
                    + W_HEAT * heat));
    return score;
  }

  private double fulfillScore(List<HomestayRentalEntity> orders, Map<Long, RefundRequestEntity> refunds) {
    List<HomestayRentalEntity> paid = new ArrayList<>();
    for (HomestayRentalEntity order : orders) {
      if (paid(order)) paid.add(order);
    }
    if (paid.isEmpty()) return MISSING;
    int checkout = 0;
    int dispute = 0;
    for (HomestayRentalEntity order : paid) {
      String status = StringUtils.defaultString(order.getOrderStatus());
      if ("已完成".equals(status)) checkout++;
      RefundRequestEntity refund = refunds.get(order.getId());
      if (refund != null) {
        String rs = StringUtils.defaultString(refund.getStatus());
        if ("平台介入".equals(rs) || "已退款".equals(rs) || "协商中".equals(rs)) dispute++;
      }
    }
    double checkoutRate = checkout / (double) paid.size();
    double disputeRate = dispute / (double) paid.size();
    double timelySum = 0;
    int stayed = 0;
    int merchantStay = 0;
    for (HomestayRentalEntity order : paid) {
      timelySum += reviewTimely(order);
      String status = StringUtils.defaultString(order.getOrderStatus());
      if (order.getCheckInAt() != null || "已入住".equals(status) || "已完成".equals(status)) {
        stayed++;
        if (order.getMerchantCheckinAt() != null) merchantStay++;
      }
    }
    double timelyRate = timelySum / paid.size();
    double merchantConfirmRate = stayed == 0 ? MISSING : merchantStay / (double) stayed;
    return clamp(0.4 * checkoutRate + 0.25 * (1.0 - disputeRate) + 0.25 * timelyRate + 0.1 * merchantConfirmRate);
  }

  private static double reviewTimely(HomestayRentalEntity order) {
    if (order.getMerchantReviewedAt() == null) return MISSING;
    Date start = order.getRentalTime() != null ? order.getRentalTime() : order.getCreateTime();
    if (start == null) return MISSING;
    long hours = Math.max(0, (order.getMerchantReviewedAt().getTime() - start.getTime()) / 3600000L);
    if (hours <= 24) return 1.0;
    if (hours <= 48) return 0.6;
    return 0.25;
  }

  private double responseScore(
      List<ConversationEntity> convs, Map<Long, List<ConversationMessageEntity>> messages) {
    if (convs == null || convs.isEmpty()) return MISSING;
    double sum = 0;
    int n = 0;
    for (ConversationEntity conv : convs) {
      List<ConversationMessageEntity> rows = messages.get(conv.getId());
      if (rows == null || rows.isEmpty()) continue;
      Date consumerAt = null;
      Date merchantAt = null;
      for (ConversationMessageEntity msg : rows) {
        if (msg.getCreateTime() == null) continue;
        if (consumerAt == null && "consumer".equals(msg.getSenderRole())) {
          consumerAt = msg.getCreateTime();
        } else if (consumerAt != null
            && merchantAt == null
            && ("merchant".equals(msg.getSenderRole()) || "admin".equals(msg.getSenderRole()))) {
          merchantAt = msg.getCreateTime();
          break;
        }
      }
      if (consumerAt == null) continue;
      n++;
      if (merchantAt == null) {
        sum += 0.12;
        continue;
      }
      long minutes = Math.max(0, (merchantAt.getTime() - consumerAt.getTime()) / 60000L);
      if (minutes <= 15) sum += 1.0;
      else if (minutes <= 60) sum += 0.8;
      else if (minutes <= 240) sum += 0.5;
      else if (minutes <= 1440) sum += 0.3;
      else sum += 0.12;
    }
    if (n == 0) return MISSING;
    return clamp(sum / n);
  }

  private IndexContext loadContext(List<PlatformViewEntity> listings) {
    IndexContext ctx = new IndexContext();
    List<Long> ids = new ArrayList<>();
    for (PlatformViewEntity listing : listings) {
      if (listing.getId() != null) ids.add(listing.getId());
    }
    if (ids.isEmpty()) return ctx;
    List<HomestayRentalEntity> orders =
        rentalService.list(
            new QueryWrapper<HomestayRentalEntity>()
                .in("homestay_id", ids)
                .eq("source_type", "platform_view"));
    for (HomestayRentalEntity order : orders) {
      if (order.getHomestayId() == null) continue;
      ctx.orders.computeIfAbsent(order.getHomestayId(), key -> new ArrayList<>()).add(order);
    }
    if (!orders.isEmpty()) {
      List<Long> orderIds = new ArrayList<>();
      for (HomestayRentalEntity order : orders) orderIds.add(order.getId());
      for (RefundRequestEntity refund :
          refundRequestService.list(new QueryWrapper<RefundRequestEntity>().in("order_id", orderIds))) {
        if (refund.getOrderId() != null) ctx.refundByOrder.put(refund.getOrderId(), refund);
      }
    }
    List<ConversationEntity> convs =
        conversationService.list(
            new QueryWrapper<ConversationEntity>().in("homestay_id", ids).eq("source_type", "platform_view"));
    List<Long> convIds = new ArrayList<>();
    for (ConversationEntity conv : convs) {
      if (conv.getHomestayId() == null) continue;
      ctx.convs.computeIfAbsent(conv.getHomestayId(), key -> new ArrayList<>()).add(conv);
      convIds.add(conv.getId());
    }
    if (!convIds.isEmpty()) {
      for (ConversationMessageEntity msg :
          conversationMessageService.list(
              new QueryWrapper<ConversationMessageEntity>().in("conversation_id", convIds).orderByAsc("id"))) {
        ctx.messages.computeIfAbsent(msg.getConversationId(), key -> new ArrayList<>()).add(msg);
      }
    }
    return ctx;
  }

  private static boolean paid(HomestayRentalEntity order) {
    if (order == null) return false;
    if (!"已支付".equals(order.getIsPaid())) return false;
    String status = StringUtils.defaultString(order.getOrderStatus());
    return !"已取消".equals(status) && !"已拒绝".equals(status);
  }

  private static double logNorm(double value, double ref) {
    return Math.log(1 + Math.max(0, value)) / Math.log(1 + ref);
  }

  private static double clamp(double value) {
    if (value < 0) return 0;
    if (value > 1) return 1;
    return value;
  }

  private static double pct(double unit) {
    return Math.round(unit * 1000.0) / 10.0;
  }

  private static double round1(double value) {
    return Math.round(value * 10.0) / 10.0;
  }

  private static class Score {
    double total;
    double rating;
    double volume;
    double fulfill;
    double response;
    double heat;
  }

  private static class IndexContext {
    Map<Long, List<HomestayRentalEntity>> orders = new LinkedHashMap<>();
    Map<Long, RefundRequestEntity> refundByOrder = new HashMap<>();
    Map<Long, List<ConversationEntity>> convs = new LinkedHashMap<>();
    Map<Long, List<ConversationMessageEntity>> messages = new HashMap<>();
  }
}
