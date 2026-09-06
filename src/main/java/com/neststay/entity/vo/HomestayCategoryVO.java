package com.neststay.entity.vo;

import java.io.Serializable;

/**
 * 民宿类型
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public class HomestayCategoryVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 图片 */
  private String image;

  /** 设置：图片 */
  public void setImage(String image) {
    this.image = image;
  }

  /** 获取：图片 */
  public String getImage() {
    return image;
  }
}
