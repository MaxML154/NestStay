package com.neststay.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 社区论坛 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class ForumPostModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 帖子内容 */
  private String content;

  /** 父节点id */
  private Long parentId;

  /** 用户id */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 头像 */
  private String avatarUrl;

  /** 状态 */
  private String isDone;

  /** 是否置顶 */
  private Integer isTop;

  /** 置顶时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date topTime;

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
}
