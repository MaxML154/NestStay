package com.neststay.entity.model;

import java.io.Serializable;

/**
 * 商家 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class MerchantModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 密码 */
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
