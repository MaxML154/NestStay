package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("support_ticket_message")
public class SupportTicketMessageEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private Long ticketId;
  private String senderRole;
  private Long senderId;
  private String senderName;
  private String content;
  private String attachments;
  private String action;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public Long getTicketId() { return ticketId; }
  public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
  public String getSenderRole() { return senderRole; }
  public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
  public Long getSenderId() { return senderId; }
  public void setSenderId(Long senderId) { this.senderId = senderId; }
  public String getSenderName() { return senderName; }
  public void setSenderName(String senderName) { this.senderName = senderName; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getAttachments() { return attachments; }
  public void setAttachments(String attachments) { this.attachments = attachments; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
}
