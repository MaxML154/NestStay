package com.neststay.utils;

import jakarta.servlet.http.HttpServletRequest;

/** Controller 层角色判断，供管理接口统一使用。 */
public final class AuthSupport {
  private AuthSupport() {}

  public static Long userId(HttpServletRequest request) {
    Object value = request.getSession().getAttribute("userId");
    if (value == null) return null;
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  public static String username(HttpServletRequest request) {
    Object value = request.getSession().getAttribute("username");
    return value == null ? null : value.toString();
  }

  public static boolean isAdmin(HttpServletRequest request) {
    return userId(request) != null
        && "管理员".equals(String.valueOf(request.getSession().getAttribute("role")))
        && "users".equals(String.valueOf(request.getSession().getAttribute("tableName")));
  }

  public static boolean isMerchant(HttpServletRequest request) {
    return userId(request) != null
        && "merchant".equals(String.valueOf(request.getSession().getAttribute("tableName")));
  }

  public static boolean isConsumer(HttpServletRequest request) {
    return userId(request) != null
        && "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")));
  }

  public static R adminError(HttpServletRequest request) {
    return userId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "仅管理员可执行此操作");
  }

  public static R staffError(HttpServletRequest request) {
    return userId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "仅管理员或商家可执行此操作");
  }
}
