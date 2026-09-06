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
 * 民宿资讯分类 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("news_category")
public class NewsCategoryEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public NewsCategoryEntity() {}

  public NewsCategoryEntity(T t) {
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

  /** 分类名称 */
  private String typeName;

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

  /** 设置：分类名称 */
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  /** 获取：分类名称 */
  public String getTypeName() {
    return typeName;
  }
}
