package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 平台民宿一览 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("platform_view")
public class PlatformViewEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public PlatformViewEntity() {}

  public PlatformViewEntity(T t) {
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

  /** 民宿名称 */
  private String homestayName;

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

  /** 审核状态 */
  private String auditStatus;

  /** NestStay Selected */
  private Integer isSelected;

  /** 1=管理员锁定精选，重算规则不再改绿标 */
  private Integer selectLock;

  private Double avgScore;
  private Integer reviewCount;
  private Double avgStay;
  private Double avgService;
  private Double avgQuality;

  private Double platformIndex;
  private Double idxRating;
  private Double idxVolume;
  private Double idxFulfill;
  private Double idxResponse;
  private Double idxHeat;

  @TableField(exist = false)
  private Boolean suggestedSelected;

  /** 字典标签 ID，不落 platform_view 表 */
  @TableField(exist = false)
  private List<Long> tagIds;

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

  /** 设置：民宿名称 */
  public void setHomestayName(String homestayName) {
    this.homestayName = homestayName;
  }

  /** 获取：民宿名称 */
  public String getHomestayName() {
    return homestayName;
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

  public Integer getSelectLock() {
    return selectLock;
  }

  public void setSelectLock(Integer selectLock) {
    this.selectLock = selectLock;
  }

  public Double getAvgScore() {
    return avgScore;
  }

  public void setAvgScore(Double avgScore) {
    this.avgScore = avgScore;
  }

  public Integer getReviewCount() {
    return reviewCount;
  }

  public void setReviewCount(Integer reviewCount) {
    this.reviewCount = reviewCount;
  }

  public Double getAvgStay() {
    return avgStay;
  }

  public void setAvgStay(Double avgStay) {
    this.avgStay = avgStay;
  }

  public Double getAvgService() {
    return avgService;
  }

  public void setAvgService(Double avgService) {
    this.avgService = avgService;
  }

  public Double getAvgQuality() {
    return avgQuality;
  }

  public void setAvgQuality(Double avgQuality) {
    this.avgQuality = avgQuality;
  }

  public Double getPlatformIndex() {
    return platformIndex;
  }

  public void setPlatformIndex(Double platformIndex) {
    this.platformIndex = platformIndex;
  }

  public Double getIdxRating() {
    return idxRating;
  }

  public void setIdxRating(Double idxRating) {
    this.idxRating = idxRating;
  }

  public Double getIdxVolume() {
    return idxVolume;
  }

  public void setIdxVolume(Double idxVolume) {
    this.idxVolume = idxVolume;
  }

  public Double getIdxFulfill() {
    return idxFulfill;
  }

  public void setIdxFulfill(Double idxFulfill) {
    this.idxFulfill = idxFulfill;
  }

  public Double getIdxResponse() {
    return idxResponse;
  }

  public void setIdxResponse(Double idxResponse) {
    this.idxResponse = idxResponse;
  }

  public Double getIdxHeat() {
    return idxHeat;
  }

  public void setIdxHeat(Double idxHeat) {
    this.idxHeat = idxHeat;
  }

  public Boolean getSuggestedSelected() {
    return suggestedSelected;
  }

  public void setSuggestedSelected(Boolean suggestedSelected) {
    this.suggestedSelected = suggestedSelected;
  }

  public List<Long> getTagIds() {
    return tagIds;
  }

  public void setTagIds(List<Long> tagIds) {
    this.tagIds = tagIds;
  }
}
