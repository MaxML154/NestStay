package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 商家 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("merchant")
public class MerchantEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public MerchantEntity() {}

  public MerchantEntity(T t) {
    try {
      BeanUtils.copyProperties(this, t);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** 主键id */
  @TableId private Long id;

  /** 商家账号 */
  private String merchantAccount;

  /** 密码 */
  @com.fasterxml.jackson.annotation.JsonProperty(
      access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
  @com.fasterxml.jackson.annotation.JsonAlias({"password", "mima"})
  private String passwordHash;

  /** 商家名称 */
  private String merchantName;

  /** 商家地址 */
  private String merchantAddress;

  /** 商家电话 */
  private String merchantPhone;

  /** 头像 */
  private String avatar;

  /** 营业执照 */
  private String businessLicense;

  /** 身份证正面 */
  private String idCardFront;

  /** 身份证反面 */
  private String idCardBack;

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

  /** 设置：商家账号 */
  public void setMerchantAccount(String merchantAccount) {
    this.merchantAccount = merchantAccount;
  }

  /** 获取：商家账号 */
  public String getMerchantAccount() {
    return merchantAccount;
  }

  /** 设置：密码 */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /** 获取：密码 */
  public String getPasswordHash() {
    return passwordHash;
  }

  /** 设置：商家名称 */
  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  /** 获取：商家名称 */
  public String getMerchantName() {
    return merchantName;
  }

  /** 设置：商家地址 */
  public void setMerchantAddress(String merchantAddress) {
    this.merchantAddress = merchantAddress;
  }

  /** 获取：商家地址 */
  public String getMerchantAddress() {
    return merchantAddress;
  }

  /** 设置：商家电话 */
  public void setMerchantPhone(String merchantPhone) {
    this.merchantPhone = merchantPhone;
  }

  /** 获取：商家电话 */
  public String getMerchantPhone() {
    return merchantPhone;
  }

  /** 设置：头像 */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  /** 获取：头像 */
  public String getAvatar() {
    return avatar;
  }

  /** 设置：营业执照 */
  public void setBusinessLicense(String businessLicense) {
    this.businessLicense = businessLicense;
  }

  /** 获取：营业执照 */
  public String getBusinessLicense() {
    return businessLicense;
  }

  /** 设置：身份证正面 */
  public void setIdCardFront(String idCardFront) {
    this.idCardFront = idCardFront;
  }

  /** 获取：身份证正面 */
  public String getIdCardFront() {
    return idCardFront;
  }

  /** 设置：身份证反面 */
  public void setIdCardBack(String idCardBack) {
    this.idCardBack = idCardBack;
  }

  /** 获取：身份证反面 */
  public String getIdCardBack() {
    return idCardBack;
  }
}
