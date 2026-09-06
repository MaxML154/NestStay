package com.neststay.utils;

import org.apache.commons.lang3.StringUtils;

/** 入库媒体路径：去掉 context、双前缀，只保留 upload/... */
public final class MediaPaths {
  private MediaPaths() {}

  public static String store(String raw) {
    if (raw == null) return null;
    String value = raw.trim();
    if (value.isEmpty()) return null;
    value = value.split(",")[0].trim().replace('\\', '/');
    int query = value.indexOf('?');
    if (query >= 0) value = value.substring(0, query);
    value = value.replaceFirst("(?i)^https?://[^/]+", "");
    while (value.startsWith("/")) {
      value = value.substring(1);
    }
    int guard = 0;
    while (guard++ < 4 && (value.equals("neststay") || value.startsWith("neststay/"))) {
      value = value.substring("neststay".length());
      while (value.startsWith("/")) {
        value = value.substring(1);
      }
    }
    while (value.startsWith("upload/upload/")) {
      value = value.substring("upload/".length());
    }
    return StringUtils.trimToNull(value);
  }
}
