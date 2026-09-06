package com.neststay.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 评价信息 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class ReviewInfoModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 民宿名称 */
  private String homestayName;

  /** 民宿图片 */
  private String homestayImage;

  /** 评分 */
  private String rating;

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
  public void setRating(String rating) {
    this.rating = rating;
  }

  /** 获取：评分 */
  public String getRating() {
    return rating;
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
  public void setCrossUserId(Long crossUserId) {
    this.crossUserId = crossUserId;
  }

  /** 获取：跨表用户id */
  public Long getCrossUserId() {
    return crossUserId;
  }

  /** 设置：跨表主键id */
  public void setCrossRefId(Long crossRefId) {
    this.crossRefId = crossRefId;
  }

  /** 获取：跨表主键id */
  public Long getCrossRefId() {
    return crossRefId;
  }
}
