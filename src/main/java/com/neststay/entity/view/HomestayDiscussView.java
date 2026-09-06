package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.HomestayDiscussEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * minsuxinxi评论表 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
@TableName("homestay_discuss")
public class HomestayDiscussView extends HomestayDiscussEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayDiscussView() {}

  public HomestayDiscussView(HomestayDiscussEntity homestayDiscussEntity) {
    try {
      BeanUtils.copyProperties(this, homestayDiscussEntity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
