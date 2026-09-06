package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.OnlineReplyEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * 在线回复 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("online_reply")
public class OnlineReplyView extends OnlineReplyEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public OnlineReplyView() {}

  public OnlineReplyView(OnlineReplyEntity onlineReplyEntity) {
    try {
      BeanUtils.copyProperties(this, onlineReplyEntity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
