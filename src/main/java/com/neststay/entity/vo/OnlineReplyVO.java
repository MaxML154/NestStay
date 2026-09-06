package com.neststay.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 在线回复
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class OnlineReplyVO implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 回复图片 */
  private String replyImage;

  /** 回复视频 */
  private String replyVideo;

  /** 回复内容 */
  private String replyContent;

  /** 回复时间 */
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date replyTime;

  /** 商家账号 */
  private String merchantAccount;

  /** 商家名称 */
  private String merchantName;

  /** 账号 */
  private String account;

  /** 姓名 */
  private String realName;

  /** 设置：回复图片 */
  public void setReplyImage(String replyImage) {
    this.replyImage = replyImage;
  }

  /** 获取：回复图片 */
  public String getReplyImage() {
    return replyImage;
  }

  /** 设置：回复视频 */
  public void setReplyVideo(String replyVideo) {
    this.replyVideo = replyVideo;
  }

  /** 获取：回复视频 */
  public String getReplyVideo() {
    return replyVideo;
  }

  /** 设置：回复内容 */
  public void setReplyContent(String replyContent) {
    this.replyContent = replyContent;
  }

  /** 获取：回复内容 */
  public String getReplyContent() {
    return replyContent;
  }

  /** 设置：回复时间 */
  public void setReplyTime(Date replyTime) {
    this.replyTime = replyTime;
  }

  /** 获取：回复时间 */
  public Date getReplyTime() {
    return replyTime;
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
