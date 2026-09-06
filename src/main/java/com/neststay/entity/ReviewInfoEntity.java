package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 评价信息 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("review_info")
public class ReviewInfoEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public ReviewInfoEntity() {}

  public ReviewInfoEntity(T t) {
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

  /** 订单编号 */
  private String orderNumber;

  /** 民宿名称 */
  private String homestayName;

  /** 民宿图片 */
  private String homestayImage;

  /** 评分 */
  private String score;

  /** 评价内容 */
  private String reviewContent;

  /** 评价时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date reviewTime;

  /** 商家账号 */
  private String merchantAccount;

  /** 商家名称 */
  private String merchantName;

  /** 商家电话 */
  private String merchantPhone;

  /** 账号 */
  private String account;

  /** 姓名 */
  private String realName;

  /** 电话 */
  private String phone;

  /** 跨表用户id */
  private Long crossUserId;

  /** 跨表主键id */
  private Long crossRefId;

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

  /** 设置：订单编号 */
  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  /** 获取：订单编号 */
  public String getOrderNumber() {
    return orderNumber;
  }

  /** 设置：民宿名称 */
  public void setHomestayName(String homestayName) {
    this.homestayName = homestayName;
  }

  /** 获取：民宿名称 */
  public String getHomestayName() {
    return homestayName;
  }

  /** 设置：民宿图片 */
  public void setHomestayImage(String homestayImage) {
    this.homestayImage = homestayImage;
  }

  /** 获取：民宿图片 */
  public String getHomestayImage() {
    return homestayImage;
  }

  /** 设置：评分 */
  public void setCreditScore(String score) {
    this.score = score;
  }

  /** 获取：评分 */
  public String getCreditScore() {
    return score;
  }

  /** 设置：评价内容 */
  public void setReviewContent(String reviewContent) {
    this.reviewContent = reviewContent;
  }

  /** 获取：评价内容 */
  public String getReviewContent() {
    return reviewContent;
  }

  /** 设置：评价时间 */
  public void setReviewTime(Date reviewTime) {
    this.reviewTime = reviewTime;
  }

  /** 获取：评价时间 */
  public Date getReviewTime() {
    return reviewTime;
  }

  /** 设置：商家账号 */
  public void setMerchantAccount(String merchantAccount) {
    this.merchantAccount = merchantAccount;
  }

  /** 获取：商家账号 */
  public String getMerchantAccount() {
    return merchantAccount;
  }

  /** 设置：商家名称 */
  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  /** 获取：商家名称 */
  public String getMerchantName() {
    return merchantName;
  }

  /** 设置：商家电话 */
  public void setMerchantPhone(String merchantPhone) {
    this.merchantPhone = merchantPhone;
  }

  /** 获取：商家电话 */
  public String getMerchantPhone() {
    return merchantPhone;
  }

  /** 设置：账号 */
  public void setAccount(String account) {
    this.account = account;
  }

  /** 获取：账号 */
  public String getAccount() {
    return account;
  }

  /** 设置：姓名 */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /** 获取：姓名 */
  public String getRealName() {
    return realName;
  }

  /** 设置：电话 */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /** 获取：电话 */
  public String getPhone() {
    return phone;
  }

  /** 设置：跨表用户id */
  public void setCrossuserId(Long crossUserId) {
    this.crossUserId = crossUserId;
  }

  /** 获取：跨表用户id */
  public Long getCrossuserId() {
    return crossUserId;
  }

  /** 设置：跨表主键id */
  public void setCrossrefId(Long crossRefId) {
    this.crossRefId = crossRefId;
  }

  /** 获取：跨表主键id */
  public Long getCrossrefId() {
    return crossRefId;
  }
}
