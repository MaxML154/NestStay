package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.HomestayRentalEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * 民宿租赁 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@TableName("homestay_rental")
public class HomestayRentalView extends HomestayRentalEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public HomestayRentalView() {}

  public HomestayRentalView(HomestayRentalEntity homestayRentalEntity) {
    try {
      BeanUtils.copyProperties(this, homestayRentalEntity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
