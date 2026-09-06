package com.neststay.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 民宿租赁 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class HomestayRentalModel implements Serializable {
  private static final long serialVersionUID = 1L;

  private String sourceType;
  private Long homestayId;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date checkInDate;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date checkOutDate;

  private Integer guestCount;
  private String orderStatus;

  /** 民宿名称 */
  private String homestayName;

  /** 民宿图片 */
  private String homestayImage;

  /** 民宿位置 */
  private String homestayLocation;

  /** 民宿类型 */
  private String homestayCategory;

  /** 价格/天 */
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
  private String realName;

  /** 电话 */
  private String phone;

  /** 是否审核 */
  private String approvalStatus;

  /** 审核回复 */
  private String approvalReply;

  /** 是否支付 */
  private String isPaid;

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public Long getHomestayId() {
    return homestayId;
  }

  public void setHomestayId(Long homestayId) {
    this.homestayId = homestayId;
  }

  public Date getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(Date checkInDate) {
    this.checkInDate = checkInDate;
  }

  public Date getCheckOutDate() {
    return checkOutDate;
  }

  public void setCheckOutDate(Date checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  public Integer getGuestCount() {
    return guestCount;
  }

  public void setGuestCount(Integer guestCount) {
    this.guestCount = guestCount;
  }

  public String getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(String orderStatus) {
    this.orderStatus = orderStatus;
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
}
