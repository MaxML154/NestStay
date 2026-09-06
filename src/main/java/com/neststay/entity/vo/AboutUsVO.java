package com.neststay.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.AboutUsEntity;
import java.io.Serializable;

/** 关于我们 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("about_us")
public class AboutUsVO extends AboutUsEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public AboutUsVO() {}
}
