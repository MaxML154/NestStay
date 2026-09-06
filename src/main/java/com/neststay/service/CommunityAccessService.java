package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.NewsAuthorApplicationEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 论坛/资讯准入、等级和完成订单统计。 */
@Service
public class CommunityAccessService {
  @Autowired private HomestayRentalService rentalService;
  @Autowired private ConsumerService consumerService;
  @Autowired private MerchantService merchantService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private ForumPostService forumPostService;
  @Autowired private NewsAuthorApplicationService newsAuthorApplicationService;

  public long completedOrders(String account) {
    if (StringUtils.isBlank(account)) return 0;
    return rentalService.count(
        new QueryWrapper<HomestayRentalEntity>()
            .eq("account", account)
            .in("order_status", "已完成", "已退房"));
  }

  public long merchantCompletedOrders(String merchantAccount) {
    if (StringUtils.isBlank(merchantAccount)) return 0;
    return rentalService.count(
        new QueryWrapper<HomestayRentalEntity>()
            .eq("merchant_account", merchantAccount)
            .in("order_status", "已完成", "已退房", "待入住", "已入住")
            .eq("is_paid", "已支付"));
  }

  public int forumLevel(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return 99;
    int computed = computeForumLevel(request);
    // TEST_OPEN: Plan 1 testing — logged-in consumer/merchant can post. Revert by returning computed.
    if ((AuthSupport.isConsumer(request) || AuthSupport.isMerchant(request)) && computed < 1) {
      return 1;
    }
    return computed;
  }

  private int computeForumLevel(HttpServletRequest request) {
    if (AuthSupport.isConsumer(request)) {
      ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
      long orders = completedOrders(AuthSupport.username(request));
      if (!isConsumerBound(consumer) || orders < 1) return 0;
      if (orders >= 10) return 3;
      long approvedPosts =
          forumPostService.count(
              new QueryWrapper<ForumPostEntity>()
                  .eq("user_id", AuthSupport.userId(request))
                  .eq("parent_id", 0)
                  .eq("audit_status", "已通过"));
      if (orders >= 3 || approvedPosts >= 2) return 2;
      return 1;
    }
    if (AuthSupport.isMerchant(request)) {
      long listings =
          platformViewService.count(
              new QueryWrapper<PlatformViewEntity>()
                  .eq("merchant_account", AuthSupport.username(request)));
      long orders = merchantCompletedOrders(AuthSupport.username(request));
      if (listings < 1 || orders < 10) return 0;
      if (orders >= 100) return 3;
      if (orders >= 30) return 2;
      return 1;
    }
    return 0;
  }

  public R requireForumAccess(HttpServletRequest request, boolean posting, int targetLevel) {
    if (AuthSupport.isAdmin(request)) return null;
    int level = forumLevel(request);
    if (level <= 0) {
      if (AuthSupport.isConsumer(request)) {
        return R.error(403, "需绑定手机号和身份证，并完成本平台一笔民宿订单后才能在论坛交流");
      }
      if (AuthSupport.isMerchant(request)) {
        return R.error(403, "需至少发布一个房源并成功交易10单后才能在论坛交流");
      }
      return R.error(401, "请先登录");
    }
    if (posting && level < 1) return R.error(403, "当前等级不能发帖");
    if (targetLevel >= 3 && level < 3 && !posting) {
      return R.error(403, "Lv1 用户不能回复 Lv3 主题");
    }
    return null;
  }

  public boolean canPublishNews(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return true;
    Long userId = AuthSupport.userId(request);
    if (userId == null) return false;
    // TEST_OPEN: Plan 1 testing — allow publishing news without author application.
    if (AuthSupport.isConsumer(request) || AuthSupport.isMerchant(request)) return true;
    return newsAuthorApplicationService.count(
            new QueryWrapper<NewsAuthorApplicationEntity>()
                .eq("user_id", userId)
                .eq("status", "已通过"))
        > 0;
  }

  public Map<String, Object> snapshot(HttpServletRequest request) {
    Map<String, Object> data = new HashMap<>();
    data.put("level", forumLevel(request));
    data.put("admin", AuthSupport.isAdmin(request));
    data.put("consumer", AuthSupport.isConsumer(request));
    data.put("merchant", AuthSupport.isMerchant(request));
    if (AuthSupport.isConsumer(request)) {
      ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
      data.put("bound", isConsumerBound(consumer));
      data.put("completedOrders", completedOrders(AuthSupport.username(request)));
    }
    if (AuthSupport.isMerchant(request)) {
      data.put("completedOrders", merchantCompletedOrders(AuthSupport.username(request)));
    }
    data.put("canPublishNews", canPublishNews(request));
    return data;
  }

  public boolean isConsumerBound(ConsumerEntity consumer) {
    return consumer != null
        && StringUtils.isNotBlank(consumer.getPhone())
        && StringUtils.isNotBlank(consumer.getIdNumber());
  }

  public java.util.List<String> homestayKeywords() {
    return Arrays.asList("民宿", "住宿", "入住", "房东", "房源", "旅行", "旅游", "度假", "房间", "客房");
  }
}
