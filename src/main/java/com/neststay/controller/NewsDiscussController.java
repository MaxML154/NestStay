package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.NewsDiscussEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.NewsArticleService;
import com.neststay.service.NewsDiscussService;
import com.neststay.service.ContentAutoAuditService;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 民宿资讯评论接口。 */
@RestController
@RequestMapping({"/newsDiscuss", "/discussnews"})
public class NewsDiscussController {
  private static final int MAX_DEPTH = 5;
  private static final int MAX_DELETE_NODES = 10000;

  @Autowired private NewsDiscussService discussService;
  @Autowired private NewsArticleService newsService;
  @Autowired private ConsumerService consumerService;
  @Autowired private ContentAutoAuditService contentAutoAuditService;

  @IgnoreAuth
  @RequestMapping("/tree/{refid}")
  public R tree(@PathVariable Long refid) {
    if (newsService.getById(refid) == null) return R.error(404, "资讯不存在");
    List<NewsDiscussEntity> roots =
        discussService.list(
            new QueryWrapper<NewsDiscussEntity>()
                .eq("refid", refid)
                .eq("parent_id", 0)
                .orderByAsc("create_time")
                .orderByAsc("id"));
    for (NewsDiscussEntity root : roots) {
      root.setDepth(0);
      buildChildren(root, 0, new HashSet<>());
    }
    return R.ok().put("data", roots);
  }

  @RequestMapping("/add")
  @Transactional
  public R add(@RequestBody NewsDiscussEntity submitted, HttpServletRequest request) {
    if (submitted == null
        || submitted.getRefid() == null
        || StringUtils.isBlank(submitted.getContent())) {
      return R.error(400, "资讯ID和评论内容不能为空");
    }
    if (newsService.getById(submitted.getRefid()) == null) {
      return R.error(404, "资讯不存在");
    }
    Long parentId = submitted.getParentId() == null ? 0L : submitted.getParentId();
    if (parentId > 0) {
      NewsDiscussEntity parent = discussService.getById(parentId);
      if (parent == null || !submitted.getRefid().equals(parent.getRefid())) {
        return R.error(400, "回复目标不属于当前资讯");
      }
      int parentDepth = resolveDepth(parent);
      if (parentDepth < 0 || parentDepth >= MAX_DEPTH) {
        return R.error(400, "评论回复不能超过" + MAX_DEPTH + "层");
      }
    }

    NewsDiscussEntity comment = new NewsDiscussEntity();
    comment.setRefid(submitted.getRefid());
    comment.setParentId(parentId);
    comment.setContent(submitted.getContent().trim());
    if (!isAdmin(request)) {
      ContentAutoAuditService.Result audit =
          contentAutoAuditService.review("", comment.getContent());
      if (audit.rejected() || audit.hold()) {
        return R.error(400, StringUtils.defaultIfBlank(audit.reason, "评论未通过自动审核"));
      }
    }

    if (isAdmin(request)) {
      comment.setUserId(currentUserId(request));
      comment.setNickname("管理员");
      discussService.save(comment);
      return R.ok().put("data", comment);
    }

    Long userId = currentConsumerId(request);
    if (userId == null) return consumerAuthError(request);
    ConsumerEntity consumer = consumerService.getById(userId);
    if (consumer == null) return R.error(404, "消费者不存在");
    comment.setUserId(userId);
    comment.setNickname(
        StringUtils.defaultIfBlank(consumer.getNickname(), consumer.getAccount()));
    comment.setAvatarUrl(consumer.getAvatar());
    discussService.save(comment);
    return R.ok().put("data", comment);
  }

  @RequestMapping("/delete")
  @Transactional
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (ids == null || ids.length == 0) return R.error(400, "请选择要删除的评论");
    Long userId = currentConsumerId(request);
    boolean admin = isAdmin(request);
    if (!admin && userId == null) return consumerAuthError(request);

    Set<Long> deleteIds = new LinkedHashSet<>();
    for (Long id : ids) {
      NewsDiscussEntity existing = discussService.getById(id);
      if (existing == null) continue;
      if (!admin && !userId.equals(existing.getUserId())) {
        return R.error(403, "无权删除其他用户的评论");
      }
      try {
        collectSubtree(id, deleteIds);
      } catch (IllegalStateException exception) {
        return R.error(400, exception.getMessage());
      }
    }
    if (deleteIds.isEmpty()) return R.error(404, "评论不存在");
    discussService.removeByIds(deleteIds);
    return R.ok();
  }

  private void buildChildren(
      NewsDiscussEntity parent, int depth, Set<Long> visited) {
    if (parent == null || parent.getId() == null || depth >= MAX_DEPTH) {
      if (parent != null) parent.setChilds(Collections.emptyList());
      return;
    }
    if (!visited.add(parent.getId())) {
      parent.setChilds(Collections.emptyList());
      return;
    }
    List<NewsDiscussEntity> children =
        discussService.list(
            new QueryWrapper<NewsDiscussEntity>()
                .eq("refid", parent.getRefid())
                .eq("parent_id", parent.getId())
                .orderByAsc("create_time")
                .orderByAsc("id"));
    parent.setChilds(children);
    for (NewsDiscussEntity child : children) {
      child.setDepth(depth + 1);
      child.setReplyToUsername(parent.getNickname());
      buildChildren(child, depth + 1, visited);
    }
    visited.remove(parent.getId());
  }

  private int resolveDepth(NewsDiscussEntity comment) {
    NewsDiscussEntity current = comment;
    Set<Long> visited = new HashSet<>();
    int depth = 0;
    while (current != null && current.getParentId() != null && current.getParentId() > 0) {
      if (current.getId() == null || !visited.add(current.getId())) return -1;
      depth++;
      if (depth > MAX_DEPTH) return -1;
      current = discussService.getById(current.getParentId());
    }
    return current == null ? -1 : depth;
  }

  private void collectSubtree(Long id, Set<Long> result) {
    Deque<Long> queue = new ArrayDeque<>();
    queue.add(id);
    while (!queue.isEmpty()) {
      Long currentId = queue.removeFirst();
      if (!result.add(currentId)) continue;
      if (result.size() > MAX_DELETE_NODES) {
        throw new IllegalStateException("单次删除评论过多");
      }
      List<NewsDiscussEntity> children =
          discussService.list(
              new QueryWrapper<NewsDiscussEntity>()
                  .select("id")
                  .eq("parent_id", currentId));
      for (NewsDiscussEntity child : children) {
        if (child.getId() != null) queue.addLast(child.getId());
      }
    }
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
        : R.error(403, "仅消费者可发表评论");
  }
}
