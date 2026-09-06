package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 民宿信息 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("homestay_info")
public class HomestayInfoEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayInfoEntity() {}

  public HomestayInfoEntity(T t) {
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

  /** 名称 */
  @TableField("display_name")
  private String name;

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
  @TableField("district")
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

  /** 宣传视频 */
  private String videoIntro;

  /** 宣传详情 */
  private String detailContent;

  /** 审核状态 */
  private String auditStatus;

  /** NestStay Selected */
  private Integer isSelected;

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

  /** 设置：名称 */
  public void setName(String name) {
    this.name = name;
  }

  /** 获取：名称 */
  public String getName() {
    return name;
  }

  /** MyBatis resultType 会把 display_name 映射成 displayName */
  public void setDisplayName(String displayName) {
    this.name = displayName;
  }

  public String getDisplayName() {
    return name;
  }

  /** 设置：封面 */
  public void setCoverImage(String coverImage) {
    this.coverImage = coverImage;
  }

  /** 获取：封面 */
  public String getCoverImage() {
    return coverImage;
  }

  /** 设置：城市 */
  public void setCity(String city) {
    this.city = city;
  }

  /** 获取：城市 */
  public String getCity() {
    return city;
  }

  /** 设置：地址 */
  public void setAddress(String address) {
    this.address = address;
  }

  /** 获取：地址 */
  public String getAddress() {
    return address;
  }

  /** 设置：原价 */
  public void setOriginalPrice(Integer originalPrice) {
    this.originalPrice = originalPrice;
  }

  /** 获取：原价 */
  public Integer getOriginalPrice() {
    return originalPrice;
  }

  /** 设置：现价 */
  public void setCurrentPrice(Integer currentPrice) {
    this.currentPrice = currentPrice;
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

  public void setDistrict(String district) {
    this.qu = district;
  }

  public String getDistrict() {
    return qu;
  }

  /** 设置：评分 */
  public void setRating(String rating) {
    this.rating = rating;
  }

  /** 获取：评分 */
  public String getRating() {
    return rating;
  }

  /** 设置：服务 */
  public void setServiceTag(String serviceTag) {
    this.serviceTag = serviceTag;
  }

  /** 获取：服务 */
  public String getServiceTag() {
    return serviceTag;
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

  /** 设置：评论数 */
  public void setDiscussCount(Integer discussCount) {
    this.discussCount = discussCount;
  }

  /** 获取：评论数 */
  public Integer getDiscussCount() {
    return discussCount;
  }

  /** 设置：收藏数 */
  public void setFavoriteCount(Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  /** 获取：收藏数 */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  public String getVideoIntro() {
    return videoIntro;
  }

  public void setVideoIntro(String videoIntro) {
    this.videoIntro = videoIntro;
  }

  public String getDetailContent() {
    return detailContent;
  }

  public void setDetailContent(String detailContent) {
    this.detailContent = detailContent;
  }

  public String getAuditStatus() {
    return auditStatus;
  }

  public void setAuditStatus(String auditStatus) {
    this.auditStatus = auditStatus;
  }

  public Integer getIsSelected() {
    return isSelected;
  }

  public void setIsSelected(Integer isSelected) {
    this.isSelected = isSelected;
  }
}
