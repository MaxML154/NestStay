package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@TableName("news_author_application")
public class NewsAuthorApplicationEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  @DateTimeFormat
  private Date createTime;

  private Long userId;
  private String userRole;
  private String account;
  private String proofImages;
  private Integer followerTotal;
  private Integer douyinFollowers;
  private Integer platformCount;
  private String status;
  private String reviewReply;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Date getCreateTime() { return createTime; }
  public void setCreateTime(Date createTime) { this.createTime = createTime; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getUserRole() { return userRole; }
  public void setUserRole(String userRole) { this.userRole = userRole; }
  public String getAccount() { return account; }
  public void setAccount(String account) { this.account = account; }
  public String getProofImages() { return proofImages; }
  public void setProofImages(String proofImages) { this.proofImages = proofImages; }
  public Integer getFollowerTotal() { return followerTotal; }
  public void setFollowerTotal(Integer followerTotal) { this.followerTotal = followerTotal; }
  public Integer getDouyinFollowers() { return douyinFollowers; }
  public void setDouyinFollowers(Integer douyinFollowers) { this.douyinFollowers = douyinFollowers; }
  public Integer getPlatformCount() { return platformCount; }
  public void setPlatformCount(Integer platformCount) { this.platformCount = platformCount; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getReviewReply() { return reviewReply; }
  public void setReviewReply(String reviewReply) { this.reviewReply = reviewReply; }
}
