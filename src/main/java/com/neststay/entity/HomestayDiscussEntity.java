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
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * minsuxinxi评论表 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
@TableName("homestay_discuss")
public class HomestayDiscussEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayDiscussEntity() {}

  public HomestayDiscussEntity(T t) {
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

  /** 关联表id */
  @TableField("refid")
  private Long refid;

  /** 用户id */
  @JsonAlias("userid")
  private Long userId;

  /** 头像 */
  @JsonAlias("avatarurl")
  private String avatarUrl;

  /** 用户名 */
  private String nickname;

  /** 评论内容 */
  private String content;

  /** 评分 */
  @TableField("credit_score")
  @JsonProperty("score")
  private Double score;

  @TableField("score_stay")
  private Double scoreStay;

  @TableField("score_service")
  private Double scoreService;

  @TableField("score_quality")
  private Double scoreQuality;

  @TableField("order_number")
  private String orderNumber;

  /** 回复内容 */
  private String reply;

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

  /** 设置：关联表id */
  public void setRefid(Long refid) {
    this.refid = refid;
  }

  /** 获取：关联表id */
  public Long getRefid() {
    return refid;
  }

  /** 设置：用户id */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /** 获取：用户id */
  public Long getUserId() {
    return userId;
  }

  /** 设置：头像 */
  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  /** 获取：头像 */
  public String getAvatarUrl() {
    return avatarUrl;
  }

  /** 设置：用户名 */
  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  /** 获取：用户名 */
  public String getNickname() {
    return nickname;
  }

  /** 设置：评论内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：评论内容 */
  public String getContent() {
    return content;
  }

  /** 设置：评分 */
  public void setScore(Double score) {
    this.score = score;
  }

  /** 获取：评分 */
  public Double getScore() {
    return score;
  }

  public Double getScoreStay() {
    return scoreStay;
  }

  public void setScoreStay(Double scoreStay) {
    this.scoreStay = scoreStay;
  }

  public Double getScoreService() {
    return scoreService;
  }

  public void setScoreService(Double scoreService) {
    this.scoreService = scoreService;
  }

  public Double getScoreQuality() {
    return scoreQuality;
  }

  public void setScoreQuality(Double scoreQuality) {
    this.scoreQuality = scoreQuality;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public void setCreditScore(Double creditScore) {
    this.score = creditScore;
  }

  /** 设置：回复内容 */
  public void setReply(String reply) {
    this.reply = reply;
  }

  /** 获取：回复内容 */
  public String getReply() {
    return reply;
  }
}
