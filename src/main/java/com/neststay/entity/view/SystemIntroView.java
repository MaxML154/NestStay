package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.SystemIntroEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/** 系统简介 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("system_intro")
public class SystemIntroView extends SystemIntroEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public SystemIntroView() {}

  public SystemIntroView(SystemIntroEntity entity) {
    try {
      BeanUtils.copyProperties(this, entity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
