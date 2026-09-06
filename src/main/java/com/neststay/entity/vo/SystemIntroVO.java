package com.neststay.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.SystemIntroEntity;
import java.io.Serializable;

/** 系统简介 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("system_intro")
public class SystemIntroVO extends SystemIntroEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public SystemIntroVO() {}
}
