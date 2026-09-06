package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("stay_service")
public class StayServiceEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private String merchantAccount;
  private String serviceName;
  private String description;
  private Integer price;
  private Integer enabled;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public String getMerchantAccount() { return merchantAccount; }
  public void setMerchantAccount(String merchantAccount) { this.merchantAccount = merchantAccount; }
  public String getServiceName() { return serviceName; }
  public void setServiceName(String serviceName) { this.serviceName = serviceName; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public Integer getPrice() { return price; }
  public void setPrice(Integer price) { this.price = price; }
  public Integer getEnabled() { return enabled; }
  public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
