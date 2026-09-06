package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.AdminUserEntity;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.NewsDiscussEntity;
import com.neststay.entity.view.NewsArticleView;
import com.neststay.service.AdminUserService;
import com.neststay.service.CommunityAccessService;
import com.neststay.service.ConsumerService;
import com.neststay.service.FavoriteService;
import com.neststay.service.NewsArticleService;
import com.neststay.service.NewsDiscussService;
import com.neststay.service.SystemNoticeService;
import com.neststay.service.ContentAutoAuditService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 民宿资讯后端接口 */
@RestController
@RequestMapping({"/newsArticle", "/news"})
public class NewsArticleController {
  @Autowired private NewsArticleService newsService;
  @Autowired private FavoriteService storeupService;
  @Autowired private NewsDiscussService newsDiscussService;
  @Autowired private AdminUserService adminUserService;
  @Autowired private CommunityAccessService communityAccessService;
  @Autowired private ConsumerService consumerService;
  @Autowired private ContentAutoAuditService contentAutoAuditService;
  @Autowired private SystemNoticeService systemNoticeService;

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      NewsArticleEntity news,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    PageUtils page =
        newsService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
    return R.ok().put("data", page);
  }

  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      NewsArticleEntity news,
      HttpServletRequest request) {
    if ("addtime".equals(params.get("sort"))) {
      params.put("sort", "create_time");
    }
    if ("hot".equals(params.get("sort"))) {
      params.put("sort", "click_count");
      params.put("order", "desc");
    }
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    if (!AuthSupport.isAdmin(request)) {
      ew.and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"));
    }
    Object typeName = params.remove("typeName");
    if (typeName != null && StringUtils.isNotBlank(typeName.toString())) {
      String value = typeName.toString();
      if (value.contains("%")) {
        ew.like("type_name", value.replace("%", ""));
      } else {
        ew.eq("type_name", value);
      }
    }
    PageUtils page =
        newsService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
    enrichAuthors(page);
    return R.ok().put("data", page);
  }

  @RequestMapping("/lists")
  public R list(NewsArticleEntity news, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(news, "news"));
    return R.ok().put("data", newsService.selectListView(ew));
  }

  @RequestMapping("/query")
  public R query(NewsArticleEntity news, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    ew.allEq(MPUtil.allEQMapPre(news, "news"));
    NewsArticleView view = newsService.selectView(ew);
    return R.ok("查询民宿资讯成功").put("data", view);
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    NewsArticleEntity news = newsService.getById(id);
    return news == null ? R.error(404, "资讯不存在") : R.ok().put("data", news);
  }

  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    return detailData(id, request);
  }

  private R detailData(Long id, HttpServletRequest request) {
    NewsArticleEntity news = newsService.getById(id);
    if (news == null) {
      return R.error(404, "资讯不存在");
    }
    Long viewerId = AuthSupport.userId(request);
    boolean owner = viewerId != null && viewerId.equals(news.getAuthorId());
    if (!AuthSupport.isAdmin(request)
        && news.getAuditStatus() != null
        && !"已通过".equals(news.getAuditStatus())
        && !owner) {
      return R.error(404, "资讯不存在");
    }
    news.setClickCount((news.getClickCount() == null ? 0 : news.getClickCount()) + 1);
    news.setClickTime(new Date());
    newsService.updateById(news);
    fillAuthor(news);
    enrichReactions(news, request);
    return R.ok().put("data", news);
  }

  @RequestMapping("/thumbsup/{id}")
  public R vote(@PathVariable("id") String id, String type) {
    return R.error(410, "旧投票接口已停用，请使用统一点赞接口");
  }

  @RequestMapping("/save")
  @Transactional
  public R save(@RequestBody NewsArticleEntity news, HttpServletRequest request) {
    return add(news, request);
  }

  @RequestMapping("/add")
  @Transactional
  public R add(@RequestBody NewsArticleEntity news, HttpServletRequest request) {
    if (news == null
        || StringUtils.isBlank(news.getTitle())
        || StringUtils.isBlank(news.getContent())) {
      return R.error(400, "资讯标题和内容不能为空");
    }
    Long adminId = currentAdminId(request);
    if (adminId == null && !communityAccessService.canPublishNews(request)) {
      return R.error(403, "请先向平台申请资讯作者资格");
    }
    news.setId(null);
    if (adminId != null) {
      if (!bindAuthor(news, adminId)) return R.error(404, "管理员账号不存在");
    } else {
      news.setAuthorId(AuthSupport.userId(request));
      news.setAuthorRole(AuthSupport.isMerchant(request) ? "商家" : "消费者");
      ConsumerEntity consumer = consumerService.getById(AuthSupport.userId(request));
      if (consumer != null) {
        news.setName(StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
        news.setHeadPortrait(consumer.getAvatar());
      } else {
        news.setName(AuthSupport.username(request));
      }
    }
    news.setClickCount(0);
    news.setThumbsUpCount(0);
    news.setDislikeCount(0);
    news.setFavoriteCount(0);
    if (adminId != null) {
      news.setAuditStatus(ContentAutoAuditService.PASS);
      news.setAuditReason("管理员发布");
    } else {
      ContentAutoAuditService.Result audit =
          contentAutoAuditService.review(news.getTitle(), news.getContent());
      if (audit.rejected()) {
        return R.error(400, audit.reason);
      }
      news.setAuditStatus(audit.status);
      news.setAuditReason(audit.reason);
    }
    newsService.save(news);
    if (adminId == null
        && news.getAuthorId() != null
        && ContentAutoAuditService.HOLD.equals(news.getAuditStatus())) {
      systemNoticeService.newsAudit(
          news.getAuthorId(), news.getTitle(), news.getAuditStatus(), news.getAuditReason(), news.getId());
    }
    return R.ok().put("data", news);
  }

  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    NewsArticleEntity news =
        newsService.getOne(new QueryWrapper<NewsArticleEntity>().eq("", username));
    return R.ok().put("data", news);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody NewsArticleEntity news, HttpServletRequest request) {
    Long adminId = currentAdminId(request);
    if (adminId == null) return adminAuthError(request);
    if (news == null || news.getId() == null) {
      return R.error(400, "资讯ID不能为空");
    }
    NewsArticleEntity existing = newsService.getById(news.getId());
    if (existing == null) return R.error(404, "资讯不存在");
    news.setAuthorId(existing.getAuthorId());
    news.setAuthorRole(existing.getAuthorRole());
    news.setName(existing.getName());
    news.setHeadPortrait(existing.getHeadPortrait());
    news.setClickCount(null);
    news.setThumbsUpCount(null);
    news.setFavoriteCount(null);
    news.setDislikeCount(null);
    newsService.updateById(news);
    return R.ok();
  }

  @RequestMapping("/audit")
  public R audit(@RequestBody NewsArticleEntity news, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (news == null || news.getId() == null) return R.error(400, "资讯ID不能为空");
    NewsArticleEntity existing = newsService.getById(news.getId());
    if (existing == null) return R.error(404, "资讯不存在");
    existing.setAuditStatus(news.getAuditStatus());
    existing.setAuditReason(news.getAuditReason());
    newsService.updateById(existing);
    systemNoticeService.newsAudit(
        existing.getAuthorId(),
        existing.getTitle(),
        existing.getAuditStatus(),
        existing.getAuditReason(),
        existing.getId());
    return R.ok();
  }

  @RequestMapping("/delete")
  @Transactional
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (currentAdminId(request) == null) return adminAuthError(request);
    if (ids == null || ids.length == 0) return R.error(400, "请选择要删除的资讯");
    newsDiscussService.remove(
        new QueryWrapper<NewsDiscussEntity>().in("refid", Arrays.asList(ids)));
    storeupService.remove(
        new QueryWrapper<FavoriteEntity>()
            .eq("table_name", "news")
            .in("refid", Arrays.asList(ids)));
    newsService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private void enrichAuthors(PageUtils page) {
    if (page == null || page.getList() == null || page.getList().isEmpty()) return;
    @SuppressWarnings("unchecked")
    List<NewsArticleEntity> list = (List<NewsArticleEntity>) page.getList();
    for (NewsArticleEntity item : list) fillAuthor(item);
  }

  private void fillAuthor(NewsArticleEntity news) {
    if (news == null || news.getAuthorId() == null || "管理员".equals(news.getAuthorRole())) return;
    ConsumerEntity consumer = consumerService.getById(news.getAuthorId());
    if (consumer == null) return;
    news.setName(StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
    if (StringUtils.isNotBlank(consumer.getAvatar())) {
      news.setHeadPortrait(consumer.getAvatar());
    }
  }

  private void enrichReactions(NewsArticleEntity news, HttpServletRequest request) {
    long likeCount = storeupService.count(reactionWrapper(news.getId(), "21"));
    long favoriteCount = storeupService.count(reactionWrapper(news.getId(), "1"));
    news.setLikeCount(likeCount);
    news.setThumbsUpCount((int) Math.min(likeCount, Integer.MAX_VALUE));
    news.setFavoriteCount((int) Math.min(favoriteCount, Integer.MAX_VALUE));
    news.setLiked(false);
    news.setFavorited(false);

    Long consumerId = currentConsumerId(request);
    if (consumerId == null) return;
    FavoriteEntity like =
        storeupService.getOne(
            reactionWrapper(news.getId(), "21").eq("user_id", consumerId));
    FavoriteEntity favorite =
        storeupService.getOne(
            reactionWrapper(news.getId(), "1").eq("user_id", consumerId));
    if (like != null) {
      news.setLiked(true);
      news.setLikeRecordId(like.getId());
    }
    if (favorite != null) {
      news.setFavorited(true);
      news.setFavoriteRecordId(favorite.getId());
    }
  }

  private QueryWrapper<FavoriteEntity> reactionWrapper(Long newsId, String type) {
    return new QueryWrapper<FavoriteEntity>()
        .eq("table_name", "news")
        .eq("refid", newsId)
        .eq("type", type);
  }

  private boolean bindAuthor(NewsArticleEntity news, Long adminId) {
    AdminUserEntity admin = adminUserService.getById(adminId);
    if (admin == null) return false;
    news.setAuthorId(adminId);
    news.setAuthorRole("管理员");
    news.setName(admin.getUsername());
    news.setHeadPortrait(admin.getImage());
    return true;
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

  private Long currentAdminId(HttpServletRequest request) {
    return "管理员".equals(String.valueOf(request.getSession().getAttribute("role")))
            && "users".equals(String.valueOf(request.getSession().getAttribute("tableName")))
        ? currentUserId(request)
        : null;
  }

  private Long currentConsumerId(HttpServletRequest request) {
    return "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")))
        ? currentUserId(request)
        : null;
  }

  private R adminAuthError(HttpServletRequest request) {
    return currentUserId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "仅管理员可管理民宿资讯");
  }

  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      NewsArticleEntity news,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    Map<String, Object> newMap = new HashMap<>();
    Map<String, Object> param = new HashMap<>();
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String newKey = entry.getKey();
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    params.put("sort", "click_count");
    params.put("order", "desc");
    PageUtils page =
        newsService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
    return R.ok().put("data", page);
  }

  @RequestMapping("/autoSort2")
  public R autoSort2(
      @RequestParam Map<String, Object> params,
      NewsArticleEntity news,
      HttpServletRequest request) {
    Object userIdAttr = request.getSession().getAttribute("userId");
    if (userIdAttr == null) {
      return R.error(401, "未登录");
    }
    List<FavoriteEntity> storeups =
        storeupService.list(
            new QueryWrapper<FavoriteEntity>()
                .eq("type", 1)
                .eq("user_id", userIdAttr.toString())
                .eq("table_name", "news")
                .orderByDesc("create_time"));
    Integer limit =
        params.get("limit") == null ? 10 : Integer.parseInt(params.get("limit").toString());
    List<NewsArticleEntity> newsList = new ArrayList<>();
    if (storeups != null) {
      for (FavoriteEntity storeup : storeups) {
        newsList.addAll(
            newsService.list(
                new QueryWrapper<NewsArticleEntity>().eq("type_name", storeup.getIntelType())));
      }
    }
    QueryWrapper<NewsArticleEntity> ew = new QueryWrapper<>();
    params.put("sort", "id");
    params.put("order", "desc");
    PageUtils page =
        newsService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, news), params), params));
    List<NewsArticleEntity> pageList = (List<NewsArticleEntity>) page.getList();
    for (NewsArticleEntity candidate : pageList) {
      if (newsList.stream().noneMatch(item -> item.getId().equals(candidate.getId()))) {
        newsList.add(candidate);
      }
      if (newsList.size() >= limit) {
        break;
      }
    }
    page.setList(newsList.size() > limit ? newsList.subList(0, limit) : newsList);
    return R.ok().put("data", page);
  }
}
