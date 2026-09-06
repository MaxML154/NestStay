package com.neststay.utils;

import org.apache.commons.lang3.StringUtils;

/** 管理端列表与导出用的姓名/手机/身份证脱敏。 */
public final class PrivacyMask {
  private PrivacyMask() {}

  public static String maskName(String name) {
    String value = StringUtils.trimToEmpty(name);
    if (value.isEmpty()) return "未填写";
    int length = value.length();
    if (length == 1) return "*";
    if (length == 2) return value.charAt(0) + "*";
    return value.charAt(0) + "*" + value.charAt(length - 1);
  }

  public static String maskPhone(String phone) {
    String digits = StringUtils.trimToEmpty(phone).replaceAll("\\D", "");
    if (digits.length() == 11) {
      return "+86 " + digits.substring(0, 3) + "****" + digits.substring(7);
    }
    if (digits.isEmpty()) return "未填写";
    return "****";
  }

  public static String maskIdNumber(String idNumber) {
    String value = StringUtils.trimToEmpty(idNumber);
    if (value.length() < 8) return StringUtils.isBlank(value) ? "未填写" : "****";
    int hide = Math.max(0, value.length() - 7);
    StringBuilder stars = new StringBuilder();
    for (int i = 0; i < hide; i++) stars.append('*');
    return value.substring(0, 3) + stars + value.substring(value.length() - 4);
  }
}
