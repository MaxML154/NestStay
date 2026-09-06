package com.neststay.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 民宿信息
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class HomestayInfoVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 封面 */
  private String coverImage;

  /** 城市 */
  private String city;

  /** 地址 */
  private String address;

  /** 原价 */
  private Integer originalPrice;

  /** 现价 */
  private Integer currentPrice;

  /** 区 */
  private String qu;

  /** 评分 */
  private String rating;

  /** 服务 */
  private String serviceTag;

  /** 最近点击时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date clickTime;

  /** 点击次数 */
  private Integer clickCount;

  /** 评论数 */
  private Integer discussCount;

  /** 收藏数 */
  private Integer favoriteCount;

  /** 设置：封面 */
  public void setCoverImage(String fengmian) {
    this.coverImage = fengmian;
  }

  /** 获取：封面 */
  public String getCoverImage() {
    return coverImage;
  }

  /** 设置：城市 */
  public void setCity(String chengshi) {
    this.city = chengshi;
  }

  /** 获取：城市 */
  public String getCity() {
    return city;
  }

  /** 设置：地址 */
  public void setAddress(String dizhi) {
    this.address = dizhi;
  }

  /** 获取：地址 */
  public String getAddress() {
    return address;
  }

  /** 设置：原价 */
  public void setOriginalPrice(Integer yuanjia) {
    this.originalPrice = yuanjia;
  }

  /** 获取：原价 */
  public Integer getOriginalPrice() {
    return originalPrice;
  }

  /** 设置：现价 */
  public void setCurrentPrice(Integer xianjia) {
    this.currentPrice = xianjia;
  }

  /** 获取：现价 */
  public Integer getCurrentPrice() {
    return currentPrice;
  }

  /** 设置：区 */
  public void setQu(String qu) {
    this.qu = qu;
  }

  /** 获取：区 */
  public String getQu() {
    return qu;
  }

  /** 设置：评分 */
  public void setRating(String dianping) {
    this.rating = dianping;
  }

  /** 获取：评分 */
  public String getRating() {
    return rating;
  }

  /** 设置：服务 */
  public void setServiceTag(String fuwu) {
    this.serviceTag = fuwu;
  }

  /** 获取：服务 */
  public String getServiceTag() {
    return serviceTag;
  }

  /** 设置：最近点击时间 */
  public void setClickTime(Date clicktime) {
    this.clickTime = clicktime;
  }

  /** 获取：最近点击时间 */
  public Date getClickTime() {
    return clickTime;
  }

  /** 设置：点击次数 */
  public void setClickCount(Integer clicknum) {
    this.clickCount = clicknum;
  }

  /** 获取：点击次数 */
  public Integer getClickCount() {
    return clickCount;
  }

  /** 设置：评论数 */
  public void setDiscussCount(Integer discussnum) {
    this.discussCount = discussnum;
  }

  /** 获取：评论数 */
  public Integer getDiscussCount() {
    return discussCount;
  }

  /** 设置：收藏数 */
  public void setFavoriteCount(Integer storeupnum) {
    this.favoriteCount = storeupnum;
  }

  /** 获取：收藏数 */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }
}
