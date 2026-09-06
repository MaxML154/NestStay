package com.neststay.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.ChatHelperEntity;
import java.io.Serializable;

/** 聊天助手表 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用） */
@TableName("chat_helper")
public class ChatHelperVO extends ChatHelperEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public ChatHelperVO() {}
}
