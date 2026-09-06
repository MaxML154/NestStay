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
 * 民宿类型 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("homestay_category")
public class HomestayCategoryEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayCategoryEntity() {}

  public HomestayCategoryEntity(T t) {
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

  /** 民宿类型 */
  private String homestayCategory;

  /** 图片 */
  @TableField("image_path")
  private String image;

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

  /** 设置：民宿类型 */
  public void setHomestayCategory(String homestayCategory) {
    this.homestayCategory = homestayCategory;
  }

  /** 获取：民宿类型 */
  public String getHomestayCategory() {
    return homestayCategory;
  }

  /** 设置：图片 */
  public void setImage(String image) {
    this.image = image;
  }

  /** 获取：图片 */
  public String getImage() {
    return image;
  }
}
