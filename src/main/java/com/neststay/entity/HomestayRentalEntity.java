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
 * 民宿租赁 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("homestay_rental")
public class HomestayRentalEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayRentalEntity() {}

  public HomestayRentalEntity(T t) {
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

  /** 民宿来源表：homestay_info 或 platform_view */
  private String sourceType;

  /** 来源民宿ID */
  private Long homestayId;

  /** 入住日期 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date checkInDate;

  /** 退房日期 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date checkOutDate;

  /** 入住人数 */
  private Integer guestCount;

  /** 订单状态 */
  private String orderStatus;

  /** 实际入住时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date checkInAt;

  /** 商家确认入住时间（住客自己点入住时为空） */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date merchantCheckinAt;

  /** 实际签退时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date checkOutAt;

  /** 商家办理签退时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date merchantCheckoutAt;

  /** 订单编号 */
  private String orderNumber;

  /** 民宿名称 */
  private String homestayName;

  /** 民宿图片 */
  private String homestayImage;

  /** 民宿位置 */
  private String homestayLocation;

  /** 民宿类型 */
  private String homestayCategory;

  /** 价格/天 */
  @JsonAlias("price")
  private Integer pricePerDay;

  /** 预订天数 */
  private Integer bookingDays;

  /** 总价格 */
  private Integer totalPrice;

  /** 租赁时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date rentalTime;

  /** 商家账号 */
  private String merchantAccount;

  /** 商家名称 */
  private String merchantName;

  /** 商家电话 */
  private String merchantPhone;

  /** 账号 */
  private String account;

  /** 姓名 */
  @JsonAlias({"name", "real_name"})
  private String realName;

  /** 电话 */
  private String phone;

  /** 是否审核 */
  private String approvalStatus;

  /** 审核回复 */
  private String approvalReply;

  /** 商家审核通过或拒绝时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date merchantReviewedAt;

  /** 是否支付 */
  @JsonAlias({"ispay", "is_paid"})
  private String isPaid;

  /** 审核通过后须在此时限前支付 */
  @JsonAlias({"pay_deadline"})
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date payDeadline;

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

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setHomestayId(Long homestayId) {
    this.homestayId = homestayId;
  }

  public Long getHomestayId() {
    return homestayId;
  }

  public void setCheckInDate(Date checkInDate) {
    this.checkInDate = checkInDate;
  }

  public Date getCheckInDate() {
    return checkInDate;
  }

  public void setCheckOutDate(Date checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  public Date getCheckOutDate() {
    return checkOutDate;
  }

  public void setGuestCount(Integer guestCount) {
    this.guestCount = guestCount;
  }

  public Integer getGuestCount() {
    return guestCount;
  }

  public void setOrderStatus(String orderStatus) {
    this.orderStatus = orderStatus;
  }

  public String getOrderStatus() {
    return orderStatus;
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

  /** 设置：民宿位置 */
  public void setHomestayLocation(String homestayLocation) {
    this.homestayLocation = homestayLocation;
  }

  /** 获取：民宿位置 */
  public String getHomestayLocation() {
    return homestayLocation;
  }

  /** 设置：民宿类型 */
  public void setHomestayCategory(String homestayCategory) {
    this.homestayCategory = homestayCategory;
  }

  /** 获取：民宿类型 */
  public String getHomestayCategory() {
    return homestayCategory;
  }

  /** 设置：价格/天 */
  public void setPricePerDay(Integer pricePerDay) {
    this.pricePerDay = pricePerDay;
  }

  /** 获取：价格/天 */
  public Integer getPricePerDay() {
    return pricePerDay;
  }

  /** 设置：预订天数 */
  public void setBookingDays(Integer bookingDays) {
    this.bookingDays = bookingDays;
  }

  /** 获取：预订天数 */
  public Integer getBookingDays() {
    return bookingDays;
  }

  /** 设置：总价格 */
  public void setTotalPrice(Integer totalPrice) {
    this.totalPrice = totalPrice;
  }

  /** 获取：总价格 */
  public Integer getTotalPrice() {
    return totalPrice;
  }

  /** 设置：租赁时间 */
  public void setRentalTime(Date rentalTime) {
    this.rentalTime = rentalTime;
  }

  /** 获取：租赁时间 */
  public Date getRentalTime() {
    return rentalTime;
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

  /** 设置：是否审核 */
  public void setApprovalStatus(String approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  /** 获取：是否审核 */
  public String getApprovalStatus() {
    return approvalStatus;
  }

  /** 设置：审核回复 */
  public void setApprovalReply(String approvalReply) {
    this.approvalReply = approvalReply;
  }

  public Date getMerchantReviewedAt() {
    return merchantReviewedAt;
  }

  public void setMerchantReviewedAt(Date merchantReviewedAt) {
    this.merchantReviewedAt = merchantReviewedAt;
  }

  /** 获取：审核回复 */
  public String getApprovalReply() {
    return approvalReply;
  }

  /** 设置：是否支付 */
  public void setIsPaid(String isPaid) {
    this.isPaid = isPaid;
  }

  /** 获取：是否支付 */
  public String getIsPaid() {
    return isPaid;
  }

  public Date getPayDeadline() {
    return payDeadline;
  }

  public void setPayDeadline(Date payDeadline) {
    this.payDeadline = payDeadline;
  }

  public Date getCheckInAt() {
    return checkInAt;
  }

  public void setCheckInAt(Date checkInAt) {
    this.checkInAt = checkInAt;
  }

  public Date getMerchantCheckinAt() {
    return merchantCheckinAt;
  }

  public void setMerchantCheckinAt(Date merchantCheckinAt) {
    this.merchantCheckinAt = merchantCheckinAt;
  }

  public Date getCheckOutAt() {
    return checkOutAt;
  }

  public void setCheckOutAt(Date checkOutAt) {
    this.checkOutAt = checkOutAt;
  }

  public Date getMerchantCheckoutAt() {
    return merchantCheckoutAt;
  }

  public void setMerchantCheckoutAt(Date merchantCheckoutAt) {
    this.merchantCheckoutAt = merchantCheckoutAt;
  }

  @TableField(exist = false)
  private String refundStatus;

  @TableField(exist = false)
  private Long refundId;

  @TableField(exist = false)
  private Integer refundRejectCount;

  @TableField(exist = false)
  private String refundReason;

  @TableField(exist = false)
  private String refundEvidence;

  @TableField(exist = false)
  private String refundAppealReason;

  public String getRefundStatus() {
    return refundStatus;
  }

  public void setRefundStatus(String refundStatus) {
    this.refundStatus = refundStatus;
  }

  public Long getRefundId() {
    return refundId;
  }

  public void setRefundId(Long refundId) {
    this.refundId = refundId;
  }

  public Integer getRefundRejectCount() {
    return refundRejectCount;
  }

  public void setRefundRejectCount(Integer refundRejectCount) {
    this.refundRejectCount = refundRejectCount;
  }

  public String getRefundReason() {
    return refundReason;
  }

  public void setRefundReason(String refundReason) {
    this.refundReason = refundReason;
  }

  public String getRefundEvidence() {
    return refundEvidence;
  }

  public void setRefundEvidence(String refundEvidence) {
    this.refundEvidence = refundEvidence;
  }

  public String getRefundAppealReason() {
    return refundAppealReason;
  }

  public void setRefundAppealReason(String refundAppealReason) {
    this.refundAppealReason = refundAppealReason;
  }
}
