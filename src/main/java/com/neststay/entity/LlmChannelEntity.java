package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("llm_channel")
public class LlmChannelEntity implements Serializable {
  public static final String OWNER_PLATFORM = "platform";
  public static final String OWNER_MERCHANT = "merchant";
  public static final String MODE_XIAOBO_ONLY = "xiaobo_only";
  public static final String MODE_LLM_ONLY = "llm_only";
  public static final String MODE_XIAOBO_THEN_LLM = "xiaobo_then_llm";

  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private String ownerType;
  private String ownerKey;
  private Integer enabled;
  private String provider;
  private String baseUrl;

  @JsonIgnore
  @TableField("api_key_enc")
  private String apiKeyEnc;

  private String modelName;
  private String systemPrompt;
  private BigDecimal temperature;
  private String assistantMode;

  @TableField(exist = false)
  private String apiKey;

  @TableField(exist = false)
  private Boolean apiKeySet;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  public String getOwnerKey() {
    return ownerKey;
  }

  public void setOwnerKey(String ownerKey) {
    this.ownerKey = ownerKey;
  }

  public Integer getEnabled() {
    return enabled;
  }

  public void setEnabled(Integer enabled) {
    this.enabled = enabled;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKeyEnc() {
    return apiKeyEnc;
  }

  public void setApiKeyEnc(String apiKeyEnc) {
    this.apiKeyEnc = apiKeyEnc;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public void setSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public BigDecimal getTemperature() {
    return temperature;
  }

  public void setTemperature(BigDecimal temperature) {
    this.temperature = temperature;
  }

  public String getAssistantMode() {
    return assistantMode;
  }

  public void setAssistantMode(String assistantMode) {
    this.assistantMode = assistantMode;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public Boolean getApiKeySet() {
    return apiKeySet;
  }

  public void setApiKeySet(Boolean apiKeySet) {
    this.apiKeySet = apiKeySet;
  }
}
