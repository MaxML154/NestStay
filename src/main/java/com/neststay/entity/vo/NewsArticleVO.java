package com.neststay.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 民宿资讯
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class NewsArticleVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 简介 */
  private String introduction;

  /** 分类名称 */
  private String typeName;

  /** 发布人 */
  private String name;

  /** 头像 */
  private String headportrait;

  /** 点击次数 */
  private Integer clickCount;

  /** 最近点击时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date clickTime;

  /** 赞 */
  private Integer thumbsupnum;

  /** 踩 */
  private Integer crazilynum;

  /** 收藏数 */
  private Integer favoriteCount;

  /** 图片 */
  private String picture;

  /** 内容 */
  private String content;

  /** 设置：简介 */
  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }

  /** 获取：简介 */
  public String getIntroduction() {
    return introduction;
  }

  /** 设置：分类名称 */
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  /** 获取：分类名称 */
  public String getTypeName() {
    return typeName;
  }

  /** 设置：发布人 */
  public void setName(String name) {
    this.name = name;
  }

  /** 获取：发布人 */
  public String getName() {
    return name;
  }

  /** 设置：头像 */
  public void setHeadportrait(String headportrait) {
    this.headportrait = headportrait;
  }

  /** 获取：头像 */
  public String getHeadportrait() {
    return headportrait;
  }

  /** 设置：点击次数 */
  public void setClickCount(Integer clicknum) {
    this.clickCount = clicknum;
  }

  /** 获取：点击次数 */
  public Integer getClickCount() {
    return clickCount;
  }

  /** 设置：最近点击时间 */
  public void setClickTime(Date clicktime) {
    this.clickTime = clicktime;
  }

  /** 获取：最近点击时间 */
  public Date getClickTime() {
    return clickTime;
  }

  /** 设置：赞 */
  public void setThumbsupnum(Integer thumbsupnum) {
    this.thumbsupnum = thumbsupnum;
  }

  /** 获取：赞 */
  public Integer getThumbsupnum() {
    return thumbsupnum;
  }

  /** 设置：踩 */
  public void setCrazilynum(Integer crazilynum) {
    this.crazilynum = crazilynum;
  }

  /** 获取：踩 */
  public Integer getCrazilynum() {
    return crazilynum;
  }

  /** 设置：收藏数 */
  public void setFavoriteCount(Integer storeupnum) {
    this.favoriteCount = storeupnum;
  }

  /** 获取：收藏数 */
  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  /** 设置：图片 */
  public void setPicture(String picture) {
    this.picture = picture;
  }

  /** 获取：图片 */
  public String getPicture() {
    return picture;
  }

  /** 设置：内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：内容 */
  public String getContent() {
    return content;
  }
}
