package com.neststay.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 在线咨询
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class OnlineInquiryVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 图片 */
  private String image;

  /** 视频 */
  private String video;

  /** 内容 */
  private String content;

  /** 沟通时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date communicationTime;

  /** 商家账号 */
  private String merchantAccount;

  /** 商家名称 */
  private String merchantName;

  /** 账号 */
  private String account;

  /** 姓名 */
  private String realName;

  /** 设置：图片 */
  public void setImage(String image) {
    this.image = image;
  }

  /** 获取：图片 */
  public String getImage() {
    return image;
  }

  /** 设置：视频 */
  public void setVideo(String video) {
    this.video = video;
  }

  /** 获取：视频 */
  public String getVideo() {
    return video;
  }

  /** 设置：内容 */
  public void setContent(String content) {
    this.content = content;
  }

  /** 获取：内容 */
  public String getContent() {
    return content;
  }

  /** 设置：沟通时间 */
  public void setCommunicationTime(Date communicationTime) {
    this.communicationTime = communicationTime;
  }

  /** 获取：沟通时间 */
  public Date getCommunicationTime() {
    return communicationTime;
  }

  /** 设置：商家账号 */
  public void setMerchantAccount(String merchantAccount) {
    this.merchantAccount = merchantAccount;
  }

  /** 获取：商家账号 */
  public String getMerchantAccount() {
    return merchantAccount;
  }

  /** 设置：商家名称 */
  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  /** 获取：商家名称 */
  public String getMerchantName() {
    return merchantName;
  }

  /** 设置：账号 */
  public void setAccount(String account) {
    this.account = account;
  }

  /** 获取：账号 */
  public String getAccount() {
    return account;
  }

  /** 设置：姓名 */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /** 获取：姓名 */
  public String getRealName() {
    return realName;
  }
}
