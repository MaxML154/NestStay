package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.NewsAuthorApplicationEntity;
import com.neststay.service.CommunityAccessService;
import com.neststay.service.NewsAuthorApplicationService;
import com.neststay.service.SystemNoticeService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/newsAuthor")
public class NewsAuthorController {
  @Autowired private NewsAuthorApplicationService applicationService;
  @Autowired private CommunityAccessService communityAccessService;
  @Autowired private SystemNoticeService systemNoticeService;

  @RequestMapping("/page")
  public R page(@RequestParam Map<String, Object> params, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    int page = 1;
    int limit = 10;
    try {
      if (params.get("page") != null) page = Integer.parseInt(params.get("page").toString());
      if (params.get("limit") != null) limit = Integer.parseInt(params.get("limit").toString());
    } catch (NumberFormatException ignored) {
    }
    Page<NewsAuthorApplicationEntity> result =
        applicationService.page(
            new Page<>(page, limit),
            new QueryWrapper<NewsAuthorApplicationEntity>().orderByDesc("id"));
    return R.ok().put("data", new PageUtils(result));
  }

  @RequestMapping("/mine")
  public R mine(HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    return R.ok()
        .put(
            "data",
            applicationService.getOne(
                new QueryWrapper<NewsAuthorApplicationEntity>()
                    .eq("user_id", AuthSupport.userId(request))
                    .orderByDesc("id")
                    .last("limit 1"),
                false));
  }

  @RequestMapping("/apply")
  public R apply(@RequestBody NewsAuthorApplicationEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request) && !AuthSupport.isMerchant(request)) {
      return R.error(403, "仅消费者或商家可申请资讯作者");
    }
    long completed =
        AuthSupport.isConsumer(request)
            ? communityAccessService.completedOrders(AuthSupport.username(request))
            : communityAccessService.merchantCompletedOrders(AuthSupport.username(request));
    int follower = entity.getFollowerTotal() == null ? 0 : entity.getFollowerTotal();
    int douyin = entity.getDouyinFollowers() == null ? 0 : entity.getDouyinFollowers();
    int platforms = entity.getPlatformCount() == null ? 0 : entity.getPlatformCount();
    if (AuthSupport.isConsumer(request)) {
      if (completed < 10) return R.error(403, "消费者需至少完成10个订单才可申请发文");
      if (platforms < 2) return R.error(403, "请提交不少于两个社交平台凭证");
      if (!(follower > 30000 || douyin > 50000)) {
        return R.error(403, "粉丝总数需大于30000，或单抖音平台大于50000");
      }
    } else {
      if (completed < 100) return R.error(403, "商家需至少完成100次订单才可申请发文");
      if (platforms < 2 || follower < 10000) {
        return R.error(403, "商家需开通至少两个社交平台且粉丝合计达到10000");
      }
    }
    entity.setId(null);
    entity.setUserId(AuthSupport.userId(request));
    entity.setAccount(AuthSupport.username(request));
    entity.setUserRole(AuthSupport.isMerchant(request) ? "商家" : "消费者");
    entity.setStatus("待审核");
    applicationService.save(entity);
    return R.ok();
  }

  @RequestMapping("/review")
  public R review(@RequestBody NewsAuthorApplicationEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    NewsAuthorApplicationEntity existing = applicationService.getById(entity.getId());
    if (existing == null) return R.error(404, "申请不存在");
    existing.setStatus(entity.getStatus());
    existing.setReviewReply(entity.getReviewReply());
    applicationService.updateById(existing);
    String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    systemNoticeService.accountNotice(
        existing.getUserId(),
        "资讯作者申请",
        "您的资讯作者申请已于" + ts + "审核为「" + existing.getStatus() + "」"
            + (entity.getReviewReply() == null ? "" : "，" + entity.getReviewReply()),
        "/index/newsAuthor");
    return R.ok();
  }

  @IgnoreAuth
  @RequestMapping("/access")
  public R access(HttpServletRequest request) {
    return R.ok().put("data", communityAccessService.snapshot(request));
  }
}
