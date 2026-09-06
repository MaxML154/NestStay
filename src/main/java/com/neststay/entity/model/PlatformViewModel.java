package com.neststay.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 平台民宿一览 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class PlatformViewModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 民宿类型 */
  private String homestayCategory;

  /** 价格/天 */
  private Integer pricePerDay;

  /** 户型 */
  private String layout;

  /** 民宿数量 */
  private Integer homestayCount;

  /** 房源介绍 */
  private String propertyIntro;

  /** 房源特色 */
  private String propertyFeatures;

  /** 民宿位置 */
  private String homestayLocation;

  /** 商家账号 */
  private String merchantAccount;

  /** 商家名称 */
  private String merchantName;

  /** 商家电话 */
  private String merchantPhone;

  /** 民宿图片 */
  private String homestayImage;

  /** 视频介绍 */
  private String videoIntro;

  /** 最近点击时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date clickTime;

  /** 点击次数 */
  private Integer clickCount;

  /** 收藏数 */
  private Integer favoriteCount;

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

  /** 设置：户型 */
  public void setLayout(String layout) {
    this.layout = layout;
  }

  /** 获取：户型 */
  public String getLayout() {
    return layout;
  }

  /** 设置：民宿数量 */
  public void setHomestayCount(Integer homestayCount) {
    this.homestayCount = homestayCount;
  }

  /** 获取：民宿数量 */
  public Integer getHomestayCount() {
    return homestayCount;
  }

  /** 设置：房源介绍 */
  public void setPropertyIntro(String propertyIntro) {
    this.propertyIntro = propertyIntro;
  }

  /** 获取：房源介绍 */
  public String getPropertyIntro() {
    return propertyIntro;
  }

  /** 设置：房源特色 */
  public void setPropertyFeatures(String propertyFeatures) {
    this.propertyFeatures = propertyFeatures;
  }

  /** 获取：房源特色 */
  public String getPropertyFeatures() {
    return propertyFeatures;
  }

  /** 设置：民宿位置 */
  public void setHomestayLocation(String homestayLocation) {
    this.homestayLocation = homestayLocation;
  }

  /** 获取：民宿位置 */
  public String getHomestayLocation() {
    return homestayLocation;
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

  /** 设置：民宿图片 */
  public void setHomestayImage(String homestayImage) {
    this.homestayImage = homestayImage;
  }

  /** 获取：民宿图片 */
  public String getHomestayImage() {
    return homestayImage;
  }

  /** 设置：视频介绍 */
  public void setVideoIntro(String videoIntro) {
    this.videoIntro = videoIntro;
  }

  /** 获取：视频介绍 */
  public String getVideoIntro() {
    return videoIntro;
  }

  /** 设置：最近点击时间 */
  public void setClickTime(Date clickTime) {
    this.clickTime = clickTime;
  }

  /** 获取：最近点击时间 */
  public Date getClickTime() {
    return clickTime;
  }

  /** 设置：点击次数 */
  public void setClickCount(Integer clickCount) {
    this.clickCount = clickCount;
  }

  /** 获取：点击次数 */
  public Integer getClickCount() {
    return clickCount;
  }

  /** 设置：收藏数 */
  public void setFavoriteCount(Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  /** 获取：收藏数 */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }
}
