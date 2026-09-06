package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.OnlineInquiryEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * 在线咨询 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("online_inquiry")
public class OnlineInquiryView extends OnlineInquiryEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public OnlineInquiryView() {}

  public OnlineInquiryView(OnlineInquiryEntity onlineInquiryEntity) {
    try {
      BeanUtils.copyProperties(this, onlineInquiryEntity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
