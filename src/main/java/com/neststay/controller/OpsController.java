package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.ConversationEntity;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.entity.StayServiceRequestEntity;
import com.neststay.service.ConversationService;
import com.neststay.service.ForumPostService;
import com.neststay.service.NewsArticleService;
import com.neststay.service.HomestayInfoService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.PlatformViewService;
import com.neststay.service.RefundRequestService;
import com.neststay.service.StayServiceRequestService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端工作台待办。平台端今日入住只读。 */
@RestController
@RequestMapping("/ops")
public class OpsController {
  @Autowired private HomestayInfoService homestayInfoService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private RefundRequestService refundRequestService;
  @Autowired private ForumPostService forumPostService;
  @Autowired private NewsArticleService newsArticleService;
  @Autowired private ConversationService conversationService;
  @Autowired private HomestayRentalService rentalService;
  @Autowired private StayServiceRequestService requestService;

  @RequestMapping("/dashboard")
  public R dashboard(HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    Map<String, Object> data = new HashMap<>();
    boolean admin = AuthSupport.isAdmin(request);
    String merchantAccount = AuthSupport.isMerchant(request) ? AuthSupport.username(request) : null;
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

    if (admin) {
      long pendingHomestay =
          homestayInfoService.count(
                  new QueryWrapper<HomestayInfoEntity>().eq("audit_status", "待审核"))
              + platformViewService.count(
                  new QueryWrapper<PlatformViewEntity>().eq("audit_status", "待审核"));
      data.put("pendingHomestay", pendingHomestay);
      data.put(
          "pendingRefund",
          refundRequestService.count(
              new QueryWrapper<RefundRequestEntity>().eq("status", "平台介入")));
      data.put(
          "pendingForum",
          forumPostService.count(
              new QueryWrapper<ForumPostEntity>()
                  .and(w -> w.eq("parent_id", 0).or().isNull("parent_id"))
                  .eq("audit_status", "待审核")));
      data.put(
          "pendingNews",
          newsArticleService.count(
              new QueryWrapper<NewsArticleEntity>().eq("audit_status", "待审核")));
      data.put(
          "unrepliedSupport",
          conversationService.count(
              new QueryWrapper<ConversationEntity>().eq("conv_type", "admin")));
    } else {
      data.put("pendingHomestay", 0);
      data.put("pendingRefund", 0);
      data.put("pendingForum", 0);
      data.put("pendingNews", 0);
      data.put("unrepliedSupport", 0);
    }

    QueryWrapper<HomestayRentalEntity> todayQuery = new QueryWrapper<>();
    todayQuery.in("order_status", "待入住", "已入住");
    todayQuery.apply(
        "date(check_in_date) <= {0} and (check_out_date is null or date(check_out_date) > {0})",
        today.toString());
    if (merchantAccount != null) todayQuery.eq("merchant_account", merchantAccount);
    data.put("todayCheckIn", rentalService.count(todayQuery));

    if (merchantAccount != null) {
      data.put(
          "pendingStay",
          rentalService.count(
              new QueryWrapper<HomestayRentalEntity>()
                  .eq("merchant_account", merchantAccount)
                  .eq("order_status", "待入住")));
      java.util.List<HomestayRentalEntity> orders =
          rentalService.list(
              new QueryWrapper<HomestayRentalEntity>()
                  .eq("merchant_account", merchantAccount)
                  .select("id"));
      if (orders.isEmpty()) {
        data.put("serviceInbox", 0);
      } else {
        data.put(
            "serviceInbox",
            requestService.count(
                new QueryWrapper<StayServiceRequestEntity>()
                    .in(
                        "order_id",
                        orders.stream()
                            .map(HomestayRentalEntity::getId)
                            .collect(java.util.stream.Collectors.toList()))
                    .eq("status", "待处理")));
      }
    }
    return R.ok().put("data", data);
  }
}
