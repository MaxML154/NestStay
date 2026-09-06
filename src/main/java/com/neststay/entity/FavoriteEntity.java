package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 收藏表 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("favorite")
public class FavoriteEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public FavoriteEntity() {}

  public FavoriteEntity(T t) {
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

  /** 用户id */
  @JsonAlias({"userid", "user_id"})
  private Long userId;

  /** 商品id */
  @TableField("refid")
  private Long refid;

  /** 表名 */
  @JsonAlias("tablename")
  private String tableName;

  /** 名称 */
  @TableField("display_name")
  private String name;

  /** 图片 */
  private String picture;

  /** 类型 */
  private String type;

  /** 推荐类型 */
  private String intelType;

  /** 备注 */
  private String remark;

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

  /** 设置：用户id */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /** 获取：用户id */
  public Long getUserId() {
    return userId;
  }

  /** 设置：商品id */
  public void setRefid(Long refid) {
    this.refid = refid;
  }

  /** 获取：商品id */
  public Long getRefid() {
    return refid;
  }

  /** 设置：表名 */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /** 获取：表名 */
  public String getTableName() {
    return tableName;
  }

  /** 设置：名称 */
  public void setName(String name) {
    this.name = name;
  }

  /** 获取：名称 */
  public String getName() {
    return name;
  }

  /** 设置：图片 */
  public void setPicture(String picture) {
    this.picture = picture;
  }

  /** 获取：图片 */
  public String getPicture() {
    return picture;
  }

  /** 设置：类型 */
  public void setType(String type) {
    this.type = type;
  }

  /** 获取：类型 */
  public String getType() {
    return type;
  }

  /** 设置：推荐类型 */
  public void setIntelType(String intelType) {
    this.intelType = intelType;
  }

  /** 获取：推荐类型 */
  public String getIntelType() {
    return intelType;
  }

  /** 设置：备注 */
  public void setRemark(String remark) {
    this.remark = remark;
  }

  /** 获取：备注 */
  public String getRemark() {
    return remark;
  }
}
