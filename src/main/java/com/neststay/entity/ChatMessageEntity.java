package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 联系平台客服 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("chat_message")
public class ChatMessageEntity<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  public ChatMessageEntity() {}

  public ChatMessageEntity(T t) {
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

  /** 用户id */
  @JsonAlias({"userid", "user_id"})
  @JsonProperty("userId")
  private Long userId;

  /** 管理员id */
  @JsonAlias({"adminid", "admin_id"})
  private Long adminId;

  /** 提问 */
  private String ask;

  /** 回复 */
  private String reply;

  /** 是否回复 */
  @JsonAlias({"isreply", "isReply"})
  @JsonProperty("isReplied")
  private Integer isReplied;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /** 设置：用户id */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setUserid(Long userid) {
    this.userId = userid;
  }

  /** 获取：用户id */
  public Long getUserId() {
    return userId;
  }

  /** 设置：管理员id */
  public void setAdminId(Long adminId) {
    this.adminId = adminId;
  }

  /** 获取：管理员id */
  public Long getAdminId() {
    return adminId;
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

  /** 设置：是否回复 */
  public void setIsReplied(Integer isReplied) {
    this.isReplied = isReplied;
  }

  public void setIsreply(Integer isreply) {
    this.isReplied = isreply;
  }

  public Integer getIsreply() {
    return isReplied;
  }

  /** 获取：是否回复 */
  public Integer getIsReplied() {
    return isReplied;
  }
}
