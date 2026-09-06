package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/** 民宿资讯评论。 */
@TableName("news_discuss")
public class NewsDiscussEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long refid;

  @JsonAlias({"parentid", "parent_id"})
  private Long parentId;

  @JsonAlias({"userid", "user_id"})
  private Long userId;

  private String nickname;

  @JsonAlias({"avatarurl", "avatar_url"})
  private String avatarUrl;

  private String content;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  @TableField(exist = false)
  private List<NewsDiscussEntity> childs;

  @TableField(exist = false)
  private Integer depth;

  @TableField(exist = false)
  private String replyToUsername;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getRefid() {
    return refid;
  }

  public void setRefid(Long refid) {
    this.refid = refid;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public List<NewsDiscussEntity> getChilds() {
    return childs;
  }

  public void setChilds(List<NewsDiscussEntity> childs) {
    this.childs = childs;
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
}
