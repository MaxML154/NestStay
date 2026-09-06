package com.neststay.entity.model;

import java.io.Serializable;

/**
 * 消费者 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class ConsumerModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 密码 */
  private String passwordHash;

  /** 姓名 */
  private String realName;

  /** 性别 */
  private String gender;

  /** 头像 */
  private String avatar;

  /** 电话 */
  private String phone;

  /** 身份证号 */
  private String idNumber;

  /** 身份证正面 */
  private String idCardFront;

  /** 身份证反面 */
  private String idCardBack;

  /** 设置：密码 */
  public void setPasswordHash(String mima) {
    this.passwordHash = mima;
  }

  /** 获取：密码 */
  public String getPasswordHash() {
    return passwordHash;
  }

  /** 设置：姓名 */
  public void setRealName(String xingming) {
    this.realName = xingming;
  }

  /** 获取：姓名 */
  public String getRealName() {
    return realName;
  }

  /** 设置：性别 */
  public void setGender(String xingbie) {
    this.gender = xingbie;
  }

  /** 获取：性别 */
  public String getGender() {
    return gender;
  }

  /** 设置：头像 */
  public void setAvatar(String touxiang) {
    this.avatar = touxiang;
  }

  /** 获取：头像 */
  public String getAvatar() {
    return avatar;
  }

  /** 设置：电话 */
  public void setPhone(String dianhua) {
    this.phone = dianhua;
  }

  /** 获取：电话 */
  public String getPhone() {
    return phone;
  }

  /** 设置：身份证号 */
  public void setIdNumber(String shenfenzhenghao) {
    this.idNumber = shenfenzhenghao;
  }

  /** 获取：身份证号 */
  public String getIdNumber() {
    return idNumber;
  }

  /** 设置：身份证正面 */
  public void setIdCardFront(String shenfenzhengzhengmian) {
    this.idCardFront = shenfenzhengzhengmian;
  }

  /** 获取：身份证正面 */
  public String getIdCardFront() {
    return idCardFront;
  }

  /** 设置：身份证反面 */
  public void setIdCardBack(String shenfenzhengfanmian) {
    this.idCardBack = shenfenzhengfanmian;
  }

  /** 获取：身份证反面 */
  public String getIdCardBack() {
    return idCardBack;
  }
}
