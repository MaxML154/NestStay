package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("support_ticket")
public class SupportTicketEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date updateTime;

  private String ticketNo;
  private String category;
  private String priority;
  private String title;
  private String content;
  private String evidence;
  private String status;
  private Long consumerId;
  private String consumerAccount;
  private String consumerName;
  private String merchantAccount;
  private Long listingId;
  private String listingName;
  private Long orderId;
  private String orderNumber;
  private Long refundId;
  private String lastReplierRole;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date lastReplyAt;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date merchantDueAt;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date closedAt;

  private String closeReason;

  @TableField(exist = false)
  private Boolean overdue;

  @TableField(exist = false)
  private Boolean canReply;

  @TableField(exist = false)
  private Boolean canEscalate;

  @TableField(exist = false)
  private Boolean canResolve;

  @TableField(exist = false)
  private Boolean canClose;

  @TableField(exist = false)
  private Boolean canReopen;

  @TableField(exist = false)
  private List<SupportTicketMessageEntity> messages;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public Date getUpdateTime() { return updateTime; }
  public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
  public String getTicketNo() { return ticketNo; }
  public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getPriority() { return priority; }
  public void setPriority(String priority) { this.priority = priority; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getEvidence() { return evidence; }
  public void setEvidence(String evidence) { this.evidence = evidence; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getConsumerId() { return consumerId; }
  public void setConsumerId(Long consumerId) { this.consumerId = consumerId; }
  public String getConsumerAccount() { return consumerAccount; }
  public void setConsumerAccount(String consumerAccount) { this.consumerAccount = consumerAccount; }
  public String getConsumerName() { return consumerName; }
  public void setConsumerName(String consumerName) { this.consumerName = consumerName; }
  public String getMerchantAccount() { return merchantAccount; }
  public void setMerchantAccount(String merchantAccount) { this.merchantAccount = merchantAccount; }
  public Long getListingId() { return listingId; }
  public void setListingId(Long listingId) { this.listingId = listingId; }
  public String getListingName() { return listingName; }
  public void setListingName(String listingName) { this.listingName = listingName; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public String getOrderNumber() { return orderNumber; }
  public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
  public Long getRefundId() { return refundId; }
  public void setRefundId(Long refundId) { this.refundId = refundId; }
  public String getLastReplierRole() { return lastReplierRole; }
  public void setLastReplierRole(String lastReplierRole) { this.lastReplierRole = lastReplierRole; }
  public Date getLastReplyAt() { return lastReplyAt; }
  public void setLastReplyAt(Date lastReplyAt) { this.lastReplyAt = lastReplyAt; }
  public Date getMerchantDueAt() { return merchantDueAt; }
  public void setMerchantDueAt(Date merchantDueAt) { this.merchantDueAt = merchantDueAt; }
  public Date getClosedAt() { return closedAt; }
  public void setClosedAt(Date closedAt) { this.closedAt = closedAt; }
  public String getCloseReason() { return closeReason; }
  public void setCloseReason(String closeReason) { this.closeReason = closeReason; }
  public Boolean getOverdue() { return overdue; }
  public void setOverdue(Boolean overdue) { this.overdue = overdue; }
  public Boolean getCanReply() { return canReply; }
  public void setCanReply(Boolean canReply) { this.canReply = canReply; }
  public Boolean getCanEscalate() { return canEscalate; }
  public void setCanEscalate(Boolean canEscalate) { this.canEscalate = canEscalate; }
  public Boolean getCanResolve() { return canResolve; }
  public void setCanResolve(Boolean canResolve) { this.canResolve = canResolve; }
  public Boolean getCanClose() { return canClose; }
  public void setCanClose(Boolean canClose) { this.canClose = canClose; }
  public Boolean getCanReopen() { return canReopen; }
  public void setCanReopen(Boolean canReopen) { this.canReopen = canReopen; }
  public List<SupportTicketMessageEntity> getMessages() { return messages; }
  public void setMessages(List<SupportTicketMessageEntity> messages) { this.messages = messages; }
}
