package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.AboutUsEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/** 关于我们 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("about_us")
public class AboutUsView extends AboutUsEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public AboutUsView() {}

  public AboutUsView(AboutUsEntity entity) {
    try {
      BeanUtils.copyProperties(this, entity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
