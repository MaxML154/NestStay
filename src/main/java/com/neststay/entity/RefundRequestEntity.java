package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("refund_request")
public class RefundRequestEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private Long conversationId;
  private Long orderId;
  private String orderNumber;
  private String consumerAccount;
  private String merchantAccount;
  private String reason;
  private String evidence;
  private String appealReason;
  private String status;
  private Integer rejectCount;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public Long getConversationId() { return conversationId; }
  public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public String getOrderNumber() { return orderNumber; }
  public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
  public String getConsumerAccount() { return consumerAccount; }
  public void setConsumerAccount(String consumerAccount) { this.consumerAccount = consumerAccount; }
  public String getMerchantAccount() { return merchantAccount; }
  public void setMerchantAccount(String merchantAccount) { this.merchantAccount = merchantAccount; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getEvidence() { return evidence; }
  public void setEvidence(String evidence) { this.evidence = evidence; }
  public String getAppealReason() { return appealReason; }
  public void setAppealReason(String appealReason) { this.appealReason = appealReason; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Integer getRejectCount() { return rejectCount; }
  public void setRejectCount(Integer rejectCount) { this.rejectCount = rejectCount; }
}
