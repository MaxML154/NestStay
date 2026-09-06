package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("conversation_message")
public class ConversationMessageEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private Long conversationId;
  private String senderRole;
  private Long senderId;
  private String content;
  private String messageType;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date readAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public Long getConversationId() { return conversationId; }
  public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
  public String getSenderRole() { return senderRole; }
  public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
  public Long getSenderId() { return senderId; }
  public void setSenderId(Long senderId) { this.senderId = senderId; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getMessageType() { return messageType; }
  public void setMessageType(String messageType) { this.messageType = messageType; }
  public Date getReadAt() { return readAt; }
  public void setReadAt(Date readAt) { this.readAt = readAt; }
}
