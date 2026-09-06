package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 聊天助手表 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("chat_helper")
public class ChatHelperEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public ChatHelperEntity() {}

  public ChatHelperEntity(T t) {
    try {
      BeanUtils.copyProperties(this, t);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** 主键id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 提问 */
  private String ask;

  /** 回复 */
  private String reply;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date addtime) {
    this.createTime = addtime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /** 设置：提问 */
  public void setAsk(String ask) {
    this.ask = ask;
  }

  /** 获取：提问 */
  public String getAsk() {
    return ask;
  }

  /** 设置：回复 */
  public void setReply(String reply) {
    this.reply = reply;
  }

  /** 获取：回复 */
  public String getReply() {
    return reply;
  }
}
