package com.neststay.entity.model;

import java.io.Serializable;

/**
 * 联系平台客服 接收传参的实体类 （实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了） 取自ModelAndView 的model名称
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public class ChatMessageModel implements Serializable {
  private static final long serialVersionUID = 1L;

  /** 管理员id */
  private Long adminId;

  /** 提问 */
  private String ask;

  /** 回复 */
  private String reply;

  /** 是否回复 */
  private Integer isReply;

  /** 设置：管理员id */
  public void setAdminId(Long adminId) {
    this.adminId = adminId;
  }

  /** 获取：管理员id */
  public Long getAdminId() {
    return adminId;
  }

  /** 设置：提问 */
  public void setAsk(String ask) {
    this.ask = ask;
  }

  /** 获取：提问 */
  public String getAsk() {
    return ask;
  }

  /** 设置：回复 */
  public void setReply(String reply) {
    this.reply = reply;
  }

  /** 获取：回复 */
  public String getReply() {
    return reply;
  }

  /** 设置：是否回复 */
  public void setIsReply(Integer isReply) {
    this.isReply = isReply;
  }

  /** 获取：是否回复 */
  public Integer getIsReply() {
    return isReply;
  }
}
