package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.view.ForumPostView;
import com.neststay.service.CommunityAccessService;
import com.neststay.service.ConsumerService;
import com.neststay.service.ContentAutoAuditService;
import com.neststay.service.FavoriteService;
import com.neststay.service.ForumPostService;
import com.neststay.service.SystemNoticeService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

/**
 * 社区论坛 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping({"/forumPost", "/forum"})
public class ForumPostController {
  private static final int MAX_REPLY_DEPTH = 3;
  private static final int MAX_DELETE_NODES = 10000;

  @Autowired private ForumPostService forumService;
  @Autowired private ConsumerService consumerService;
  @Autowired private FavoriteService favoriteService;
  @Autowired private CommunityAccessService communityAccessService;
  @Autowired private SystemNoticeService systemNoticeService;
  @Autowired private ContentAutoAuditService contentAutoAuditService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params, ForumPostEntity forum, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    Object roleAttr = request.getSession().getAttribute("role");
    if (roleAttr != null && !roleAttr.toString().equals("管理员")) {
      Object userIdAttr = request.getSession().getAttribute("userId");
      if (userIdAttr != null) {
        forum.setUserId((Long) userIdAttr);
      }
    }
    QueryWrapper<ForumPostEntity> ew = new QueryWrapper<ForumPostEntity>();

    PageUtils page =
        forumService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, forum), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params, ForumPostEntity forum, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId == null) return consumerAuthError(request);
    forum.setUserId(userId);
    forum.setParentId(0L);
    QueryWrapper<ForumPostEntity> ew = new QueryWrapper<ForumPostEntity>();

    PageUtils page =
        forumService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, forum), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @IgnoreAuth
  @RequestMapping("/flist")
  public R flist(
      @RequestParam Map<String, Object> params, ForumPostEntity forum, HttpServletRequest request) {
    forum.setParentId(0L);
    if (!"待审核".equals(forum.getAuditStatus()) && !AuthSupport.isAdmin(request)) {
      forum.setAuditStatus("已通过");
    }
    String sort = String.valueOf(params.getOrDefault("sort", "new"));
    int pageNo = parseInt(params.get("page"), 1);
    int limit = parseInt(params.get("limit"), 10);
    QueryWrapper<ForumPostEntity> ew = new QueryWrapper<ForumPostEntity>();
    boolean computed =
        "hot".equals(sort) || "reply".equals(sort) || "unreplied".equals(sort);
    if (!computed) {
      params.put("sort", "create_time");
      params.put("order", "desc");
      PageUtils page =
          forumService.queryPage(
              params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, forum), params), params));
      @SuppressWarnings("unchecked")
      List<ForumPostEntity> records = (List<ForumPostEntity>) page.getList();
      enrichTopicList(records);
      return R.ok().put("data", page);
    }
    QueryWrapper<ForumPostEntity> filter =
        MPUtil.between(MPUtil.likeOrEq(ew, forum), params);
    filter.orderByDesc("id");
    List<ForumPostEntity> all = forumService.list(filter);
    enrichTopicList(all);
    if ("hot".equals(sort)) {
      all.sort(
          (a, b) -> {
            int sa = a.getHotScore() == null ? 0 : a.getHotScore();
            int sb = b.getHotScore() == null ? 0 : b.getHotScore();
            if (sa != sb) return Integer.compare(sb, sa);
            long ia = a.getId() == null ? 0L : a.getId();
            long ib = b.getId() == null ? 0L : b.getId();
            return Long.compare(ib, ia);
          });
    } else if ("reply".equals(sort)) {
      all.sort((a, b) -> compareTime(b.getLastReplyTime(), a.getLastReplyTime()));
    } else {
      all =
          all.stream()
              .filter(item -> item.getReplyCount() == null || item.getReplyCount() == 0)
              .collect(Collectors.toList());
      all.sort((a, b) -> compareTime(b.getCreateTime(), a.getCreateTime()));
    }
    int from = Math.max(0, (pageNo - 1) * limit);
    int to = Math.min(all.size(), from + limit);
    List<ForumPostEntity> slice = from >= all.size() ? Collections.emptyList() : all.subList(from, to);
    return R.ok().put("data", new PageUtils(new ArrayList<>(slice), all.size(), limit, pageNo));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(ForumPostEntity forum, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<ForumPostEntity> ew = new QueryWrapper<ForumPostEntity>();
    ew.allEq(MPUtil.allEQMapPre(forum, "forum"));
    ForumPostView forumView = forumService.selectView(ew);
    return R.ok("查询社区论坛成功").put("data", forumView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    ForumPostEntity forum = forumService.getById(id);
    return R.ok().put("data", forum);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    ForumPostEntity forum = forumService.getById(id);
    return R.ok().put("data", forum);
  }

  /** 论坛详情 */
  @IgnoreAuth
  @RequestMapping("/list/{id}")
  public R list(@PathVariable("id") Long id, HttpServletRequest request) {
    ForumPostEntity forum = forumService.getById(id);
    if (forum == null || (forum.getParentId() != null && forum.getParentId() != 0)) {
      return R.error(404, "帖子不存在");
    }
    forum.setDepth(0);
    buildChildren(forum, 0, new HashSet<>());
    List<ForumPostEntity> nodes = new ArrayList<>();
    collectNodes(forum, nodes);
    fillIdentities(nodes);
    enrichTopicList(Collections.singletonList(forum));
    enrichReactions(forum, request);
    return R.ok().put("data", forum);
  }

  private void buildChildren(ForumPostEntity forum, int depth, Set<Long> visited) {
    if (forum == null || forum.getId() == null || depth >= MAX_REPLY_DEPTH) {
      if (forum != null) forum.setChilds(Collections.emptyList());
      return;
    }
    if (!visited.add(forum.getId())) {
      forum.setChilds(Collections.emptyList());
      return;
    }
    List<ForumPostEntity> childs =
        forumService.list(
            new QueryWrapper<ForumPostEntity>()
                .eq("parent_id", forum.getId())
                .and(w -> w.eq("audit_status", "已通过").or().isNull("audit_status"))
                .orderByAsc("create_time")
                .orderByAsc("id"));
    forum.setChilds(childs);
    for (ForumPostEntity forumEntity : childs) {
      forumEntity.setDepth(depth + 1);
      forumEntity.setRootId(depth == 0 ? forum.getId() : forum.getRootId());
      forumEntity.setReplyToUsername(forum.getUsername());
      buildChildren(forumEntity, depth + 1, visited);
    }
    visited.remove(forum.getId());
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody ForumPostEntity forum, HttpServletRequest request) {
    return add(forum, request);
  }

  /** 前端保存 */
  @RequestMapping("/add")
  @Transactional
  public R add(@RequestBody ForumPostEntity forum, HttpServletRequest request) {
    if (forum == null || StringUtils.isBlank(forum.getContent())) {
      return R.error(400, "帖子内容不能为空");
    }
    boolean isTopic = forum.getParentId() == null || forum.getParentId() == 0;
    R denied = communityAccessService.requireForumAccess(request, isTopic, 1);
    if (denied != null) return denied;
    if (AuthSupport.isAdmin(request)) {
      forum.setId(null);
      forum.setUserId(AuthSupport.userId(request));
      forum.setUsername(AuthSupport.username(request));
      forum.setAuthorRole("管理员");
      forum.setAuthorLevel(99);
      forum.setAuditStatus("已通过");
      if (isTopic) {
        forum.setParentId(0L);
        forum.setIsDone(StringUtils.defaultIfBlank(forum.getIsDone(), "开放"));
      }
      forumService.save(forum);
      return R.ok().put("data", forum);
    }
    Long userId = AuthSupport.userId(request);
    int level = communityAccessService.forumLevel(request);
    forum.setId(null);
    forum.setUserId(userId);
    forum.setAuthorLevel(level);
    forum.setAuthorRole(AuthSupport.isMerchant(request) ? "商家" : "消费者");
    if (AuthSupport.isConsumer(request)) {
      ConsumerEntity consumer = consumerService.getById(userId);
      if (consumer != null) {
        forum.setUsername(
            StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
        forum.setAvatarUrl(consumer.getAvatar());
      } else {
        forum.setUsername(AuthSupport.username(request));
      }
    } else {
      forum.setUsername(AuthSupport.username(request));
    }
    forum.setContent(forum.getContent().trim());
    if (isTopic) {
      if (StringUtils.isBlank(forum.getTitle())) {
        return R.error(400, "帖子标题不能为空");
      }
      if (level < 1) return R.error(403, "当前等级不能发帖");
      forum.setParentId(0L);
      forum.setTitle(forum.getTitle().trim());
      forum.setIsDone(StringUtils.defaultIfBlank(forum.getIsDone(), "开放"));
      forum.setIsTop(0);
      forum.setTopTime(null);
      ContentAutoAuditService.Result audit =
          contentAutoAuditService.review(forum.getTitle(), forum.getContent());
      if (audit.rejected()) {
        return R.error(400, audit.reason);
      }
      forum.setAuditStatus(audit.status);
      forum.setAuditReason(audit.reason);
      if (StringUtils.isBlank(forum.getNodeName())) {
        forum.setNodeName("交流");
      }
      String combined = forum.getTitle() + forum.getContent();
      boolean related =
          communityAccessService.homestayKeywords().stream().anyMatch(combined::contains);
      if (!related) {
        return R.error(400, "帖子内容需与民宿或旅行相关，请修改后提交");
      }
      if (level <= 1
          && (forum.getContent().contains("<img")
              || forum.getContent().contains("<video")
              || forum.getContent().replaceAll("<[^>]+>", "").length() > 400)) {
        return R.error(403, "Lv1 仅可发布纯文字短帖");
      }
      if (level == 2 && forum.getContent().contains("<video")) {
        return R.error(403, "当前等级不能发布视频帖");
      }
    } else {
      ForumPostEntity parent = forumService.getById(forum.getParentId());
      if (parent == null) return R.error(404, "回复的评论不存在");
      AncestorInfo ancestorInfo = resolveAncestors(parent);
      if (ancestorInfo == null) return R.error(409, "评论层级数据异常");
      if (ancestorInfo.depth >= MAX_REPLY_DEPTH) {
        return R.error(400, "回复层级不能超过" + MAX_REPLY_DEPTH + "层");
      }
      ForumPostEntity root = forumService.getById(ancestorInfo.rootId);
      int targetLevel = root != null && root.getAuthorLevel() != null ? root.getAuthorLevel() : 1;
      R replyDenied = communityAccessService.requireForumAccess(request, false, targetLevel);
      if (replyDenied != null) return replyDenied;
      if (forum.getRootId() == null || !forum.getRootId().equals(ancestorInfo.rootId)) {
        return R.error(400, "回复目标不属于当前帖子");
      }
      forum.setTitle(null);
      forum.setIsDone("开放");
      forum.setIsTop(0);
      forum.setTopTime(null);
      ContentAutoAuditService.Result replyAudit =
          contentAutoAuditService.review("", forum.getContent());
      if (replyAudit.rejected()) {
        return R.error(400, replyAudit.reason);
      }
      forum.setAuditStatus(replyAudit.status);
      forum.setAuditReason(replyAudit.reason);
    }
    forumService.save(forum);
    if (isTopic && ContentAutoAuditService.HOLD.equals(forum.getAuditStatus())) {
      systemNoticeService.forumHold(
          forum.getUserId(), forum.getTitle(), forum.getAuditReason(), forum.getId());
    }
    return R.ok().put("data", forum);
  }

  /** 获取用户密保 */
  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    ForumPostEntity forum =
        forumService.getOne(new QueryWrapper<ForumPostEntity>().eq("", username));
    return R.ok().put("data", forum);
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody ForumPostEntity forum, HttpServletRequest request) {
    if (forum == null || forum.getId() == null || StringUtils.isBlank(forum.getContent())) {
      return R.error(400, "帖子ID和内容不能为空");
    }
    ForumPostEntity existing = forumService.getById(forum.getId());
    if (existing == null) return R.error(404, "帖子不存在");
    Long userId = currentConsumerId(request);
    boolean admin = isAdmin(request);
    if (!admin && (userId == null || !userId.equals(existing.getUserId()))) {
      return currentUserId(request) == null
          ? R.error(401, "请先登录")
          : R.error(403, "无权修改该内容");
    }

    ForumPostEntity changes = new ForumPostEntity();
    changes.setId(existing.getId());
    changes.setContent(forum.getContent().trim());
    if (existing.getParentId() == null || existing.getParentId() == 0) {
      if (StringUtils.isBlank(forum.getTitle())) return R.error(400, "帖子标题不能为空");
      changes.setTitle(forum.getTitle().trim());
      changes.setIsDone(StringUtils.defaultIfBlank(forum.getIsDone(), existing.getIsDone()));
      if (admin) {
        changes.setIsTop(forum.getIsTop());
        changes.setTopTime(forum.getTopTime());
      }
    }
    forumService.updateById(changes);
    return R.ok();
  }

  @RequestMapping("/audit")
  public R audit(@RequestBody ForumPostEntity forum, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    ForumPostEntity existing = forumService.getById(forum.getId());
    if (existing == null) return R.error(404, "帖子不存在");
    existing.setAuditStatus(forum.getAuditStatus());
    forumService.updateById(existing);
    boolean passed = "已通过".equals(forum.getAuditStatus());
    systemNoticeService.forumAudit(
        existing.getUserId(),
        existing.getTitle(),
        passed,
        forum.getAuditReason(),
        existing.getId());
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  @Transactional
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (ids == null || ids.length == 0) return R.error(400, "请选择要删除的内容");
    Long userId = currentConsumerId(request);
    boolean admin = isAdmin(request);
    if (!admin && userId == null) return consumerAuthError(request);

    Set<Long> deleteIds = new LinkedHashSet<>();
    for (Long id : ids) {
      ForumPostEntity existing = forumService.getById(id);
      if (existing == null) continue;
      if (!admin && !userId.equals(existing.getUserId())) {
        return R.error(403, "无权删除其他用户的内容");
      }
      try {
        collectSubtreeIds(id, deleteIds);
      } catch (IllegalStateException exception) {
        return R.error(400, exception.getMessage());
      }
    }
    if (deleteIds.isEmpty()) return R.error(404, "内容不存在");
    favoriteService.remove(
        new QueryWrapper<FavoriteEntity>()
            .eq("table_name", "forum")
            .in("refid", deleteIds));
    forumService.removeByIds(deleteIds);
    return R.ok();
  }

  private void enrichTopicList(List<ForumPostEntity> topics) {
    if (topics == null || topics.isEmpty()) return;
    fillIdentities(topics);
    List<Long> topicIds =
        topics.stream().map(ForumPostEntity::getId).filter(id -> id != null).collect(Collectors.toList());
    if (topicIds.isEmpty()) return;
    List<ForumPostEntity> replies =
        forumService.list(
            new QueryWrapper<ForumPostEntity>()
                .ne("parent_id", 0)
                .select("id", "parent_id", "user_id", "username", "avatar_url", "create_time"));
    fillIdentities(replies);
    Map<Long, List<ForumPostEntity>> children = new HashMap<>();
    for (ForumPostEntity reply : replies) {
      if (reply.getParentId() == null) continue;
      children.computeIfAbsent(reply.getParentId(), key -> new ArrayList<>()).add(reply);
    }
    Date weekAgo = Date.from(Instant.now().minus(7, ChronoUnit.DAYS));
    Map<Long, int[]> favCounts = new HashMap<>();
    List<FavoriteEntity> favs =
        favoriteService.list(
            new QueryWrapper<FavoriteEntity>()
                .eq("table_name", "forum")
                .in("refid", topicIds)
                .in("type", Arrays.asList("1", "21"))
                .ge("create_time", weekAgo)
                .select("refid", "type"));
    for (FavoriteEntity fav : favs) {
      int[] pair = favCounts.computeIfAbsent(fav.getRefid(), key -> new int[2]);
      if ("21".equals(fav.getType())) pair[0]++;
      else pair[1]++;
    }
    Map<Long, Long> likeTotals = new HashMap<>();
    for (FavoriteEntity fav :
        favoriteService.list(
            new QueryWrapper<FavoriteEntity>()
                .eq("table_name", "forum")
                .in("refid", topicIds)
                .eq("type", "21")
                .select("refid"))) {
      likeTotals.merge(fav.getRefid(), 1L, Long::sum);
    }
    for (ForumPostEntity topic : topics) {
      List<ForumPostEntity> descendants = new ArrayList<>();
      Deque<Long> queue = new ArrayDeque<>();
      queue.add(topic.getId());
      while (!queue.isEmpty()) {
        Long current = queue.removeFirst();
        List<ForumPostEntity> kids = children.get(current);
        if (kids == null) continue;
        for (ForumPostEntity kid : kids) {
          descendants.add(kid);
          if (kid.getId() != null) queue.addLast(kid.getId());
        }
      }
      topic.setReplyCount((long) descendants.size());
      topic.setLikeCount(likeTotals.getOrDefault(topic.getId(), 0L));
      ForumPostEntity last = null;
      int reply7d = 0;
      for (ForumPostEntity item : descendants) {
        if (item.getCreateTime() != null && !item.getCreateTime().before(weekAgo)) reply7d++;
        if (last == null
            || (item.getCreateTime() != null
                && (last.getCreateTime() == null || item.getCreateTime().after(last.getCreateTime())))) {
          last = item;
        }
      }
      if (last != null) {
        topic.setLastReplyUsername(
            StringUtils.defaultIfBlank(last.getNickname(), last.getUsername()));
        topic.setLastReplyAvatarUrl(last.getAvatarUrl());
        topic.setLastReplyTime(last.getCreateTime());
      } else {
        topic.setLastReplyUsername(
            StringUtils.defaultIfBlank(topic.getNickname(), topic.getUsername()));
        topic.setLastReplyAvatarUrl(topic.getAvatarUrl());
        topic.setLastReplyTime(topic.getCreateTime());
      }
      int[] pair = favCounts.getOrDefault(topic.getId(), new int[2]);
      int score = reply7d * 2 + pair[0] + pair[1];
      topic.setHotScore(score);
      topic.setHot(reply7d >= 3);
      LinkedHashSet<Long> seen = new LinkedHashSet<>();
      List<Map<String, Object>> people = new ArrayList<>();
      addParticipant(people, seen, topic);
      for (ForumPostEntity item : descendants) {
        if (people.size() >= 5) break;
        addParticipant(people, seen, item);
      }
      topic.setParticipants(people);
      if (StringUtils.isBlank(topic.getNodeName())) topic.setNodeName("交流");
    }
  }

  private void addParticipant(
      List<Map<String, Object>> people, Set<Long> seen, ForumPostEntity post) {
    if (post.getUserId() == null || !seen.add(post.getUserId())) return;
    Map<String, Object> row = new HashMap<>();
    row.put("userId", post.getUserId());
    row.put("nickname", StringUtils.defaultIfBlank(post.getNickname(), post.getUsername()));
    row.put("avatarUrl", post.getAvatarUrl());
    people.add(row);
  }

  private void collectNodes(ForumPostEntity node, List<ForumPostEntity> out) {
    if (node == null) return;
    out.add(node);
    if (node.getChilds() == null) return;
    for (Object child : node.getChilds()) {
      if (child instanceof ForumPostEntity) collectNodes((ForumPostEntity) child, out);
    }
  }

  private int compareTime(Date left, Date right) {
    if (left == null && right == null) return 0;
    if (left == null) return -1;
    if (right == null) return 1;
    return left.compareTo(right);
  }

  private void fillIdentities(List<ForumPostEntity> posts) {
    if (posts == null || posts.isEmpty()) return;
    Set<Long> ids =
        posts.stream()
            .map(ForumPostEntity::getUserId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    if (ids.isEmpty()) return;
    Map<Long, ConsumerEntity> consumers = new HashMap<>();
    for (ConsumerEntity consumer : consumerService.listByIds(ids)) {
      consumers.put(consumer.getId(), consumer);
    }
    for (ForumPostEntity post : posts) {
      ConsumerEntity consumer = consumers.get(post.getUserId());
      if (consumer == null) {
        if (StringUtils.isBlank(post.getNickname())) post.setNickname(post.getUsername());
        continue;
      }
      String nick = StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount());
      post.setNickname(nick);
      post.setUsername(nick);
      if (StringUtils.isNotBlank(consumer.getAvatar())) post.setAvatarUrl(consumer.getAvatar());
    }
  }

  private int parseInt(Object value, int fallback) {
    try {
      return value == null ? fallback : Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private AncestorInfo resolveAncestors(ForumPostEntity node) {
    ForumPostEntity current = node;
    Set<Long> visited = new HashSet<>();
    int depth = 0;
    while (current != null && current.getParentId() != null && current.getParentId() > 0) {
      if (current.getId() == null || !visited.add(current.getId())) return null;
      depth++;
      if (depth > MAX_REPLY_DEPTH) return null;
      current = forumService.getById(current.getParentId());
    }
    if (current == null || current.getId() == null) return null;
    return new AncestorInfo(current.getId(), depth);
  }

  private void collectSubtreeIds(Long rootId, Set<Long> result) {
    Deque<Long> queue = new ArrayDeque<>();
    queue.add(rootId);
    while (!queue.isEmpty()) {
      Long currentId = queue.removeFirst();
      if (!result.add(currentId)) continue;
      if (result.size() > MAX_DELETE_NODES) {
        throw new IllegalStateException("单次删除内容过多");
      }
      List<ForumPostEntity> children =
          forumService.list(
              new QueryWrapper<ForumPostEntity>()
                  .select("id")
                  .eq("parent_id", currentId));
      for (ForumPostEntity child : children) {
        if (child.getId() != null) queue.addLast(child.getId());
      }
    }
  }

  private void enrichReactions(ForumPostEntity forum, HttpServletRequest request) {
    forum.setLikeCount(
        favoriteService.count(reactionWrapper(forum.getId(), "21").select("id")));
    forum.setFavoriteCount(
        favoriteService.count(reactionWrapper(forum.getId(), "1").select("id")));
    forum.setLiked(false);
    forum.setFavorited(false);

    Long userId = currentConsumerId(request);
    if (userId == null) return;
    FavoriteEntity like =
        favoriteService.getOne(
            reactionWrapper(forum.getId(), "21").eq("user_id", userId));
    FavoriteEntity favorite =
        favoriteService.getOne(
            reactionWrapper(forum.getId(), "1").eq("user_id", userId));
    if (like != null) {
      forum.setLiked(true);
      forum.setLikeRecordId(like.getId());
    }
    if (favorite != null) {
      forum.setFavorited(true);
      forum.setFavoriteRecordId(favorite.getId());
    }
  }

  private QueryWrapper<FavoriteEntity> reactionWrapper(Long forumId, String type) {
    return new QueryWrapper<FavoriteEntity>()
        .eq("table_name", "forum")
        .eq("refid", forumId)
        .eq("type", type);
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

  private Long currentConsumerId(HttpServletRequest request) {
    return "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")))
        ? currentUserId(request)
        : null;
  }

  private boolean isAdmin(HttpServletRequest request) {
    return "管理员".equals(String.valueOf(request.getSession().getAttribute("role")));
  }

  private R consumerAuthError(HttpServletRequest request) {
    return currentUserId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "仅消费者可操作论坛内容");
  }

  private static class AncestorInfo {
    private final Long rootId;
    private final int depth;

    private AncestorInfo(Long rootId, int depth) {
      this.rootId = rootId;
      this.depth = depth;
    }
  }

  /** 前端智能排序 */
  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      ForumPostEntity forum,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<ForumPostEntity> ew = new QueryWrapper<ForumPostEntity>();
    Map<String, Object> newMap = new HashMap<String, Object>();
    Map<String, Object> param = new HashMap<String, Object>();
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String key = entry.getKey();
      String newKey = entry.getKey();
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    params.put("sort", "create_time");
    params.put("order", "desc");
    PageUtils page =
        forumService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, forum), params), params));
    return R.ok().put("data", page);
  }
}
