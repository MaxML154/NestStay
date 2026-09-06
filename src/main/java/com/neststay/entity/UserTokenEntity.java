package com.neststay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/** token表 */
@TableName("user_token")
public class UserTokenEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 表名 */
  private String tableName;

  /** 角色 */
  private String role;

  /** token */
  @TableField("user_token")
  private String token;

  /** 过期时间 */
  @TableField("expire_time")
  private Date expiratedtime;

  /** 新增时间 */
  private Date createTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getExpireTime() {
    return expiratedtime;
  }

  public void setExpireTime(Date expiratedtime) {
    this.expiratedtime = expiratedtime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserTokenEntity(
      Long userId,
      String username,
      String tableName,
      String role,
      String token,
      Date expiratedtime) {
    super();
    this.userId = userId;
    this.username = username;
    this.tableName = tableName;
    this.role = role;
    this.token = token;
    this.expiratedtime = expiratedtime;
  }

  public UserTokenEntity() {}
}
