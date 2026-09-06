package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 民宿资讯 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("news_article")
public class NewsArticleEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public NewsArticleEntity() {}

  public NewsArticleEntity(T t) {
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

  /** 标题 */
  private String title;

  /** 简介 */
  private String introduction;

  /** 分类名称 */
  @JsonAlias("typename")
  private String typeName;

  /** 发布人 */
  @TableField("display_name")
  private String name;

  /** 头像 */
  @JsonAlias("headportrait")
  private String headPortrait;

  /** 作者账号ID（历史数据可为空）。 */
  private Long authorId;

  /** 作者角色。 */
  private String authorRole;

  /** 点击次数 */
  @JsonAlias("clicknum")
  private Integer clickCount;

  /** 最近点击时间 */
  @JsonAlias("clicktime")
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date clickTime;

  /** 赞 */
  @JsonAlias("thumbsupnum")
  private Integer thumbsUpCount;

  /** 踩 */
  @JsonAlias("crazilynum")
  private Integer dislikeCount;

  /** 收藏数 */
  @JsonAlias("storeupnum")
  private Integer favoriteCount;

  /** 图片 */
  private String picture;

  /** 内容 */
  private String content;

  /** 审核：已通过 / 待审核 / 已驳回 */
  private String auditStatus;

  private String auditReason;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  @com.fasterxml.jackson.annotation.JsonProperty("addtime")
  @com.fasterxml.jackson.annotation.JsonAlias({"addtime", "createTime"})
  private Date createTime;

  @TableField(exist = false)
  private Long likeCount;

  @TableField(exist = false)
  private Boolean liked;

  @TableField(exist = false)
  private Boolean favorited;

  @TableField(exist = false)
  private Long likeRecordId;

  @TableField(exist = false)
  private Long favoriteRecordId;

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

  /** 设置：标题 */
  public void setTitle(String title) {
    this.title = title;
  }

  /** 获取：标题 */
  public String getTitle() {
    return title;
  }

  /** 设置：简介 */
  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }

  /** 获取：简介 */
  public String getIntroduction() {
    return introduction;
  }

  /** 设置：分类名称 */
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  /** 获取：分类名称 */
  public String getTypeName() {
    return typeName;
  }

  /** 设置：发布人 */
  public void setName(String name) {
    this.name = name;
  }

  /** 获取：发布人 */
  public String getName() {
    return name;
  }

  /** 设置：头像 */
  public void setHeadPortrait(String headPortrait) {
    this.headPortrait = headPortrait;
  }

  /** 获取：头像 */
  public String getHeadPortrait() {
    return headPortrait;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public String getAuthorRole() {
    return authorRole;
  }

  public void setAuthorRole(String authorRole) {
    this.authorRole = authorRole;
  }

  /** 设置：点击次数 */
  public void setClickCount(Integer clickCount) {
    this.clickCount = clickCount;
  }

  /** 获取：点击次数 */
  public Integer getClickCount() {
    return clickCount;
  }

  /** 设置：最近点击时间 */
  public void setClickTime(Date clickTime) {
    this.clickTime = clickTime;
  }

  /** 获取：最近点击时间 */
  public Date getClickTime() {
    return clickTime;
  }

  /** 设置：赞 */
  public void setThumbsUpCount(Integer thumbsUpCount) {
    this.thumbsUpCount = thumbsUpCount;
  }

  /** 获取：赞 */
  public Integer getThumbsUpCount() {
    return thumbsUpCount;
  }

  /** 设置：踩 */
  public void setDislikeCount(Integer dislikeCount) {
    this.dislikeCount = dislikeCount;
  }

  /** 获取：踩 */
  public Integer getDislikeCount() {
    return dislikeCount;
  }

  /** 设置：收藏数 */
  public void setFavoriteCount(Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  /** 获取：收藏数 */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  /** 设置：图片 */
  public void setPicture(String picture) {
    this.picture = picture;
  }

  /** 获取：图片 */
  public String getPicture() {
    return picture;
  }

  /** 设置：内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：内容 */
  public String getContent() {
    return content;
  }

  public String getAuditStatus() {
    return auditStatus;
  }

  public void setAuditStatus(String auditStatus) {
    this.auditStatus = auditStatus;
  }

  public String getAuditReason() {
    return auditReason;
  }

  public void setAuditReason(String auditReason) {
    this.auditReason = auditReason;
  }

  public Long getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Long likeCount) {
    this.likeCount = likeCount;
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
}
