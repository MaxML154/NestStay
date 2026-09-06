package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.ChatHelperEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/** 聊天助手表 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("chat_helper")
public class ChatHelperView extends ChatHelperEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public ChatHelperView() {}

  public ChatHelperView(ChatHelperEntity entity) {
    try {
      BeanUtils.copyProperties(this, entity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
