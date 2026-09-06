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
 * 消费者 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("consumer")
public class ConsumerEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public ConsumerEntity() {}

  public ConsumerEntity(T t) {
    try {
      BeanUtils.copyProperties(this, t);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** 主键id */
  @TableId private Long id;

  /** 账号 */
  private String account;

  /** 密码 */
  @com.fasterxml.jackson.annotation.JsonProperty(
      access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
  @com.fasterxml.jackson.annotation.JsonAlias({"password", "mima"})
  private String passwordHash;

  /** 用户名/昵称 */
  @com.fasterxml.jackson.annotation.JsonAlias({"userName"})
  private String nickname;

  /** 姓名 */
  @com.fasterxml.jackson.annotation.JsonAlias({"name", "xingming"})
  private String realName;

  /** 简介 */
  private String bio;

  /** 主页封面 */
  private String profileCover;

  /** image / video */
  private String profileCoverType;

  /** 他人可看发帖 */
  private Integer privacyPosts;

  /** 他人可看发文 */
  private Integer privacyArticles;

  /** 他人可看收藏 */
  private Integer privacyFavorites;

  /** 性别 */
  private String gender;

  /** 头像 */
  private String avatar;

  /** 电话 */
  private String phone;

  /** 身份证号 */
  @com.fasterxml.jackson.annotation.JsonAlias({"idCardNumber", "shenfenzhenghao"})
  private String idNumber;

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

  /** 设置：账号 */
  public void setAccount(String account) {
    this.account = account;
  }

  /** 获取：账号 */
  public String getAccount() {
    return account;
  }

  /** 设置：密码 */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /** 获取：密码 */
  public String getPasswordHash() {
    return passwordHash;
  }

  /** 设置：姓名 */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /** 获取：姓名 */
  public String getRealName() {
    return realName;
  }

  /** 设置：性别 */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /** 获取：性别 */
  public String getGender() {
    return gender;
  }

  /** 设置：头像 */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  /** 获取：头像 */
  public String getAvatar() {
    return avatar;
  }

  /** 设置：电话 */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /** 获取：电话 */
  public String getPhone() {
    return phone;
  }

  /** 设置：身份证号 */
  public void setIdNumber(String idNumber) {
    this.idNumber = idNumber;
  }

  /** 获取：身份证号 */
  public String getIdNumber() {
    return idNumber;
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

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getProfileCover() {
    return profileCover;
  }

  public void setProfileCover(String profileCover) {
    this.profileCover = profileCover;
  }

  public String getProfileCoverType() {
    return profileCoverType;
  }

  public void setProfileCoverType(String profileCoverType) {
    this.profileCoverType = profileCoverType;
  }

  public Integer getPrivacyPosts() {
    return privacyPosts;
  }

  public void setPrivacyPosts(Integer privacyPosts) {
    this.privacyPosts = privacyPosts;
  }

  public Integer getPrivacyArticles() {
    return privacyArticles;
  }

  public void setPrivacyArticles(Integer privacyArticles) {
    this.privacyArticles = privacyArticles;
  }

  public Integer getPrivacyFavorites() {
    return privacyFavorites;
  }

  public void setPrivacyFavorites(Integer privacyFavorites) {
    this.privacyFavorites = privacyFavorites;
  }
}
