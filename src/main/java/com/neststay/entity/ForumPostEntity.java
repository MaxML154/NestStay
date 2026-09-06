package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 社区论坛 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("forum_post")
public class ForumPostEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public ForumPostEntity() {}

  public ForumPostEntity(T t) {
    try {
      BeanUtils.copyProperties(this, t);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** 主键id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 帖子标题 */
  private String title;

  /** 帖子内容 */
  private String content;

  /** 父节点id */
  @JsonAlias({"parentid", "parent_id"})
  private Long parentId;

  /** 用户id */
  @JsonAlias({"userid", "user_id"})
  private Long userId;

  /** 用户名 */
  private String username;

  /** 头像 */
  @JsonAlias({"avatarurl", "avatar_url"})
  private String avatarUrl;

  /** 状态 */
  @JsonAlias({"isdone", "is_done"})
  private String isDone;

  /** 是否置顶 */
  @JsonAlias({"istop", "is_top"})
  private Integer isTop;

  /** 置顶时间 */
  @JsonAlias({"toptime", "top_time"})
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date topTime;

  /** 审核状态 */
  private String auditStatus;

  private String authorRole;
  private Integer authorLevel;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @TableField(exist = false)
  private List<ForumPostEntity> childs;

  /** 回复提交时用于校验父节点是否属于当前主题。 */
  @TableField(exist = false)
  @JsonAlias("rootid")
  private Long rootId;

  @TableField(exist = false)
  private Integer depth;

  @TableField(exist = false)
  private String replyToUsername;

  @TableField(exist = false)
  private Long likeCount;

  @TableField(exist = false)
  private Long favoriteCount;

  @TableField(exist = false)
  private Boolean liked;

  @TableField(exist = false)
  private Boolean favorited;

  @TableField(exist = false)
  private Long likeRecordId;

  @TableField(exist = false)
  private Long favoriteRecordId;

  public List<ForumPostEntity> getChilds() {
    return childs;
  }

  public void setChilds(List<ForumPostEntity> childs) {
    this.childs = childs;
  }

  public Long getRootId() {
    return rootId;
  }

  public void setRootId(Long rootId) {
    this.rootId = rootId;
  }

  public Integer getDepth() {
    return depth;
  }

  public void setDepth(Integer depth) {
    this.depth = depth;
  }

  public String getReplyToUsername() {
    return replyToUsername;
  }

  public void setReplyToUsername(String replyToUsername) {
    this.replyToUsername = replyToUsername;
  }

  public Long getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Long likeCount) {
    this.likeCount = likeCount;
  }

  public Long getFavoriteCount() {
    return favoriteCount;
  }

  public void setFavoriteCount(Long favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  public Boolean getLiked() {
    return liked;
  }

  public void setLiked(Boolean liked) {
    this.liked = liked;
  }

  public Boolean getFavorited() {
    return favorited;
  }

  public void setFavorited(Boolean favorited) {
    this.favorited = favorited;
  }

  public Long getLikeRecordId() {
    return likeRecordId;
  }

  public void setLikeRecordId(Long likeRecordId) {
    this.likeRecordId = likeRecordId;
  }

  public Long getFavoriteRecordId() {
    return favoriteRecordId;
  }

  public void setFavoriteRecordId(Long favoriteRecordId) {
    this.favoriteRecordId = favoriteRecordId;
  }

  /** 设置：帖子标题 */
  public void setTitle(String title) {
    this.title = title;
  }

  /** 获取：帖子标题 */
  public String getTitle() {
    return title;
  }

  /** 设置：帖子内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：帖子内容 */
  public String getContent() {
    return content;
  }

  /** 设置：父节点id */
  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  /** 获取：父节点id */
  public Long getParentId() {
    return parentId;
  }

  /** 设置：用户id */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /** 获取：用户id */
  public Long getUserId() {
    return userId;
  }

  /** 设置：用户名 */
  public void setUsername(String username) {
    this.username = username;
  }

  /** 获取：用户名 */
  public String getUsername() {
    return username;
  }

  /** 设置：头像 */
  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  /** 获取：头像 */
  public String getAvatarUrl() {
    return avatarUrl;
  }

  /** 设置：状态 */
  public void setIsDone(String isDone) {
    this.isDone = isDone;
  }

  /** 获取：状态 */
  public String getIsDone() {
    return isDone;
  }

  /** 设置：是否置顶 */
  public void setIsTop(Integer isTop) {
    this.isTop = isTop;
  }

  /** 获取：是否置顶 */
  public Integer getIsTop() {
    return isTop;
  }

  /** 设置：置顶时间 */
  public void setTopTime(Date topTime) {
    this.topTime = topTime;
  }

  /** 获取：置顶时间 */
  public Date getTopTime() {
    return topTime;
  }

  public String getAuditStatus() {
    return auditStatus;
  }

  public void setAuditStatus(String auditStatus) {
    this.auditStatus = auditStatus;
  }

  public String getAuthorRole() {
    return authorRole;
  }

  public void setAuthorRole(String authorRole) {
    this.authorRole = authorRole;
  }

  public Integer getAuthorLevel() {
    return authorLevel;
  }

  public void setAuthorLevel(Integer authorLevel) {
    this.authorLevel = authorLevel;
  }

  /** 论坛节点：入住体验 / 行程攻略 / 周边玩乐 等 */
  @JsonAlias("nodename")
  private String nodeName;

  @TableField(exist = false)
  private String nickname;

  @TableField(exist = false)
  private Boolean hot;

  @TableField(exist = false)
  private Integer hotScore;

  @TableField(exist = false)
  private Long replyCount;

  @TableField(exist = false)
  private String lastReplyUsername;

  @TableField(exist = false)
  private String lastReplyAvatarUrl;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @TableField(exist = false)
  private Date lastReplyTime;

  @TableField(exist = false)
  private List<Map<String, Object>> participants;

  @TableField(exist = false)
  private String auditReason;

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public Boolean getHot() {
    return hot;
  }

  public void setHot(Boolean hot) {
    this.hot = hot;
  }

  public Integer getHotScore() {
    return hotScore;
  }

  public void setHotScore(Integer hotScore) {
    this.hotScore = hotScore;
  }

  public Long getReplyCount() {
    return replyCount;
  }

  public void setReplyCount(Long replyCount) {
    this.replyCount = replyCount;
  }

  public String getLastReplyUsername() {
    return lastReplyUsername;
  }

  public void setLastReplyUsername(String lastReplyUsername) {
    this.lastReplyUsername = lastReplyUsername;
  }

  public String getLastReplyAvatarUrl() {
    return lastReplyAvatarUrl;
  }

  public void setLastReplyAvatarUrl(String lastReplyAvatarUrl) {
    this.lastReplyAvatarUrl = lastReplyAvatarUrl;
  }

  public Date getLastReplyTime() {
    return lastReplyTime;
  }

  public void setLastReplyTime(Date lastReplyTime) {
    this.lastReplyTime = lastReplyTime;
  }

  public List<Map<String, Object>> getParticipants() {
    return participants;
  }

  public void setParticipants(List<Map<String, Object>> participants) {
    this.participants = participants;
  }

  public String getAuditReason() {
    return auditReason;
  }

  public void setAuditReason(String auditReason) {
    this.auditReason = auditReason;
  }
}
