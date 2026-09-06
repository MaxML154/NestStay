package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("conversation")
public class ConversationEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private String convType;
  private Long consumerId;
  private String consumerAccount;
  private String merchantAccount;
  private Long adminId;
  private Long homestayId;
  private String sourceType;
  private String homestayName;
  private String homestayImage;
  private Integer pricePerDay;
  private String lastMessage;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date lastTime;

  private Integer deletedByConsumer;
  private Integer deletedByPeer;

  @TableField(exist = false)
  private String merchantName;

  @TableField(exist = false)
  private String merchantAvatar;

  @TableField(exist = false)
  private String consumerNickname;

  @TableField(exist = false)
  private String consumerAvatar;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public String getConvType() { return convType; }
  public void setConvType(String convType) { this.convType = convType; }
  public Long getConsumerId() { return consumerId; }
  public void setConsumerId(Long consumerId) { this.consumerId = consumerId; }
  public String getConsumerAccount() { return consumerAccount; }
  public void setConsumerAccount(String consumerAccount) { this.consumerAccount = consumerAccount; }
  public String getMerchantAccount() { return merchantAccount; }
  public void setMerchantAccount(String merchantAccount) { this.merchantAccount = merchantAccount; }
  public Long getAdminId() { return adminId; }
  public void setAdminId(Long adminId) { this.adminId = adminId; }
  public Long getHomestayId() { return homestayId; }
  public void setHomestayId(Long homestayId) { this.homestayId = homestayId; }
  public String getSourceType() { return sourceType; }
  public void setSourceType(String sourceType) { this.sourceType = sourceType; }
  public String getHomestayName() { return homestayName; }
  public void setHomestayName(String homestayName) { this.homestayName = homestayName; }
  public String getHomestayImage() { return homestayImage; }
  public void setHomestayImage(String homestayImage) { this.homestayImage = homestayImage; }
  public Integer getPricePerDay() { return pricePerDay; }
  public void setPricePerDay(Integer pricePerDay) { this.pricePerDay = pricePerDay; }
  public String getLastMessage() { return lastMessage; }
  public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
  public Date getLastTime() { return lastTime; }
  public void setLastTime(Date lastTime) { this.lastTime = lastTime; }
  public Integer getDeletedByConsumer() { return deletedByConsumer; }
  public void setDeletedByConsumer(Integer deletedByConsumer) { this.deletedByConsumer = deletedByConsumer; }
  public Integer getDeletedByPeer() { return deletedByPeer; }
  public void setDeletedByPeer(Integer deletedByPeer) { this.deletedByPeer = deletedByPeer; }
  public String getMerchantName() { return merchantName; }
  public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
  public String getMerchantAvatar() { return merchantAvatar; }
  public void setMerchantAvatar(String merchantAvatar) { this.merchantAvatar = merchantAvatar; }
  public String getConsumerNickname() { return consumerNickname; }
  public void setConsumerNickname(String consumerNickname) { this.consumerNickname = consumerNickname; }
  public String getConsumerAvatar() { return consumerAvatar; }
  public void setConsumerAvatar(String consumerAvatar) { this.consumerAvatar = consumerAvatar; }
}
