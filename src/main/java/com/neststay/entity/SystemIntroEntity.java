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
 * 系统简介 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
@TableName("system_intro")
public class SystemIntroEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public SystemIntroEntity() {}

  public SystemIntroEntity(T t) {
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

  /** 标题 */
  private String title;

  /** 副标题 */
  private String subtitle;

  /** 内容 */
  private String content;

  /** 图片1 */
  private String picture1;

  /** 图片2 */
  private String picture2;

  /** 图片3 */
  private String picture3;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date addtime) {
    this.createTime = addtime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /** 设置：标题 */
  public void setTitle(String title) {
    this.title = title;
  }

  /** 获取：标题 */
  public String getTitle() {
    return title;
  }

  /** 设置：副标题 */
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  /** 获取：副标题 */
  public String getSubtitle() {
    return subtitle;
  }

  /** 设置：内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：内容 */
  public String getContent() {
    return content;
  }

  /** 设置：图片1 */
  public void setPicture1(String picture1) {
    this.picture1 = picture1;
  }

  /** 获取：图片1 */
  public String getPicture1() {
    return picture1;
  }

  /** 设置：图片2 */
  public void setPicture2(String picture2) {
    this.picture2 = picture2;
  }

  /** 获取：图片2 */
  public String getPicture2() {
    return picture2;
  }

  /** 设置：图片3 */
  public void setPicture3(String picture3) {
    this.picture3 = picture3;
  }

  /** 获取：图片3 */
  public String getPicture3() {
    return picture3;
  }
}
