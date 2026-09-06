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
 * 租赁取消 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("rental_cancel")
public class RentalCancelEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public RentalCancelEntity() {}

  public RentalCancelEntity(T t) {
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

  /** 取消原因 */
  private String cancelReason;

  /** 取消时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date cancelTime;

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

  /** 设置：取消原因 */
  public void setCancelReason(String cancelReason) {
    this.cancelReason = cancelReason;
  }

  /** 获取：取消原因 */
  public String getCancelReason() {
    return cancelReason;
  }

  /** 设置：取消时间 */
  public void setCancelTime(Date cancelTime) {
    this.cancelTime = cancelTime;
  }

  /** 获取：取消时间 */
  public Date getCancelTime() {
    return cancelTime;
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
