package com.neststay.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

/** Mybatis-Plus工具类 */
public class MPUtil {
  public static final char UNDERLINE = '_';

  /** 缓存实体的属性名→列名映射，避免每次反射 */
  private static final Map<Class<?>, Map<String, String>> TABLE_FIELD_CACHE =
      new ConcurrentHashMap<>();

  /** 兼容旧前端/模板中的表名别名。 */
  public static String normalizeTableName(String tableName) {
    if (tableName == null) return null;
    switch (tableName) {
      case "homestayInfo":
        return "homestay_info";
      case "platformView":
        return "platform_view";
      case "homestayRental":
        return "homestay_rental";
      case "homestayCategory":
        return "homestay_category";
      case "newsCategory":
        return "news_category";
      case "chatMessage":
        return "chat_message";
      case "onlineInquiry":
        return "online_inquiry";
      case "onlineReply":
        return "online_reply";
      case "systemIntro":
        return "system_intro";
      case "reviewInfo":
        return "review_info";
      case "rentalCancel":
        return "rental_cancel";
      default:
        return tableName;
    }
  }

  /** 兼容旧前端/模板中的字段名别名。 */
  public static String normalizeColumnName(String tableName, String columnName) {
    if (columnName == null) return null;
    String normalizedTable = normalizeTableName(tableName);
    if ("homestay_info".equals(normalizedTable)) {
      if ("mingcheng".equals(columnName)) return "display_name";
      if ("chengshi".equals(columnName)) return "city";
      if ("xianjia".equals(columnName)) return "current_price";
    }
    if ("platform_view".equals(normalizedTable)) {
      if ("huxing".equals(columnName)) return "layout";
      if ("minsushuliang".equals(columnName)) return "homestay_count";
    }
    if ("news_category".equals(normalizedTable) && "typename".equals(columnName)) {
      return "type_name";
    }
    if (columnName.indexOf('_') >= 0) return columnName;
    return camelToUnderline(columnName);
  }

  public static String normalizeColumnName(Class<?> entityClass, String columnName) {
    if (entityClass == null) return columnName;
    String tableName = null;
    com.baomidou.mybatisplus.annotation.TableName tableNameAnnotation =
        entityClass.getAnnotation(com.baomidou.mybatisplus.annotation.TableName.class);
    if (tableNameAnnotation != null) {
      tableName = tableNameAnnotation.value();
    }
    String legacyColumn = normalizeColumnName(tableName, columnName);
    if (!legacyColumn.equals(columnName)) return legacyColumn;
    return fieldToColumn(entityClass, columnName);
  }

  public static void aliasMapKey(
      List<Map<String, Object>> result, String originalKey, String actualKey) {
    if (result == null || StringUtils.isBlank(originalKey) || StringUtils.isBlank(actualKey)) return;
    if (originalKey.equals(actualKey)) return;
    for (Map<String, Object> m : result) {
      if (m.containsKey(actualKey) && !m.containsKey(originalKey)) {
        m.put(originalKey, m.get(actualKey));
      }
    }
  }

  /** 获取实体属性名对应的实际数据库列名。 如果有 @TableField 自定义值则用自定义值，否则执行驼峰→下划线转换。 */
  public static String fieldToColumn(Class<?> entityClass, String fieldName) {
    // 兼容历史表字段：favorite.refid / homestay_discuss.refid 无下划线
    if (("refId".equals(fieldName) || "refid".equals(fieldName))
        && ("FavoriteEntity".equals(entityClass.getSimpleName())
            || "HomestayDiscussEntity".equals(entityClass.getSimpleName()))) {
      return "refid";
    }
    Map<String, String> fieldMap = TABLE_FIELD_CACHE.get(entityClass);
    if (fieldMap == null) {
      fieldMap = new HashMap<>();
      com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo =
          TableInfoHelper.getTableInfo(entityClass);
      if (tableInfo != null) {
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
          // property → column 映射
          fieldMap.put(fieldInfo.getProperty(), fieldInfo.getColumn());
        }
      }
      TABLE_FIELD_CACHE.put(entityClass, fieldMap);
    }
    // 如果缓存中有精确映射则使用，否则 fallback 到驼峰→下划线
    return fieldMap.getOrDefault(fieldName, camelToUnderline(fieldName));
  }

  /** 将 Map 中的实体属性名全部转为数据库列名（支持 @TableField 自定义映射） */
  public static Map<String, Object> mapFieldsToColumns(
      Class<?> entityClass, Map<String, Object> fieldMap) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
      result.put(fieldToColumn(entityClass, entry.getKey()), entry.getValue());
    }
    return result;
  }

  // mybatis plus allEQ 表达式转换（支持 @TableField 自定义列名，跳过 null 值避免 SQL 语法错误）
  public static Map allEQMapPre(Object bean, String pre) {
    Map<String, Object> map = BeanUtil.beanToMap(bean);
    Map<String, Object> newMap = new HashMap<String, Object>();
    Class<?> entityClass = bean.getClass();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null) continue;
      String newKey = fieldToColumn(entityClass, entry.getKey());
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    return newMap;
  }

  // mybatis plus allEQ 表达式转换（支持 @TableField 自定义列名，跳过 null 值避免 SQL 语法错误）
  public static Map allEQMap(Object bean) {
    Map<String, Object> map = BeanUtil.beanToMap(bean);
    Map<String, Object> newMap = new HashMap<String, Object>();
    Class<?> entityClass = bean.getClass();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null) continue;
      String newKey = fieldToColumn(entityClass, entry.getKey());
      newMap.put(newKey, entry.getValue());
    }
    return newMap;
  }

  public static QueryWrapper allLikePre(QueryWrapper wrapper, Object bean, String pre) {
    Map<String, Object> map = BeanUtil.beanToMap(bean);
    Map<String, Object> result = new HashMap<String, Object>();
    Class<?> entityClass = bean.getClass();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null) continue;
      String newKey = fieldToColumn(entityClass, entry.getKey());
      if (pre.endsWith(".")) {
        result.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        result.put(newKey, entry.getValue());
      } else {
        result.put(pre + "." + newKey, entry.getValue());
      }
    }
    return genLike(wrapper, result);
  }

  public static QueryWrapper allLike(QueryWrapper wrapper, Object bean) {
    Map<String, Object> rawMap = BeanUtil.beanToMap(bean, true, false);
    Map<String, Object> result = mapFieldsToColumns(bean.getClass(), rawMap);
    return genLike(wrapper, result);
  }

  /** 归一化 wrapper 条件列名：含大写(驼峰)则转下划线，否则原样返回。 幂等，已是下划线的列名不受影响。 */
  private static String normalizeWrapperKey(String key) {
    if (key == null) return null;
    for (int i = 0; i < key.length(); i++) {
      if (Character.isUpperCase(key.charAt(i))) {
        return camelToUnderline(key);
      }
    }
    return key;
  }

  public static QueryWrapper genLike(QueryWrapper wrapper, Map param) {
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    int i = 0;
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      if (entry.getValue() == null) continue;
      String key = normalizeWrapperKey(entry.getKey());
      String value = (String) entry.getValue();
      wrapper.like(key, value);
      i++;
    }
    return wrapper;
  }

  public static QueryWrapper likeOrEq(QueryWrapper wrapper, Object bean) {
    Map<String, Object> rawMap = BeanUtil.beanToMap(bean, true, false);
    Map<String, Object> result = mapFieldsToColumns(bean.getClass(), rawMap);
    return genLikeOrEq(wrapper, result);
  }

  public static QueryWrapper genLikeOrEq(QueryWrapper wrapper, Map param) {
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    int i = 0;
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      if (entry.getValue() == null) continue;
      String key = normalizeWrapperKey(entry.getKey());
      if (entry.getValue().toString().contains("%")) {
        wrapper.like(key, entry.getValue().toString().replace("%", ""));
      } else {
        wrapper.eq(key, entry.getValue());
      }
      i++;
    }
    return wrapper;
  }

  public static QueryWrapper allEq(QueryWrapper wrapper, Object bean) {
    Map<String, Object> rawMap = BeanUtil.beanToMap(bean, true, false);
    Map<String, Object> result = mapFieldsToColumns(bean.getClass(), rawMap);
    return genEq(wrapper, result);
  }

  public static QueryWrapper genEq(QueryWrapper wrapper, Map param) {
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    int i = 0;
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      if (entry.getValue() == null) continue;
      String key = normalizeWrapperKey(entry.getKey());
      wrapper.eq(key, entry.getValue());
      i++;
    }
    return wrapper;
  }

  public static QueryWrapper between(QueryWrapper wrapper, Map<String, Object> params) {
    for (String key : params.keySet()) {
      String columnName = "";
      if (key.endsWith("_start")) {
        columnName = key.substring(0, key.indexOf("_start"));
        if (StringUtils.isNotBlank(params.get(key).toString())) {
          wrapper.ge(columnName, params.get(key));
        }
      }
      if (key.endsWith("_end")) {
        columnName = key.substring(0, key.indexOf("_end"));
        if (StringUtils.isNotBlank(params.get(key).toString())) {
          wrapper.le(columnName, params.get(key));
        }
      }
    }
    return wrapper;
  }

  /**
   * 归一化排序/条件列名。 兼容旧前端传入的遗留列名与驼峰列名，转换为当前数据库真实列名。 未匹配的名称统一执行驼峰→下划线转换（已是下划线的保持不变）。
   */
  public static String normalizeSortColumn(String column) {
    if (column == null) return null;
    String key = column.trim();
    if (key.isEmpty()) return key;
    switch (key) {
      case "istop":
        return "is_top";
      case "toptime":
        return "top_time";
      case "addtime":
        return "create_time";
      case "price":
        // 旧前端平台民宿排序用 price，真实列名为 price_per_day（其余表无裸 price 列）
        return "price_per_day";
      default:
        return camelToUnderline(key);
    }
  }

  public static QueryWrapper sort2(QueryWrapper wrapper, Map<String, Object> params) {
    String order = "";
    if (params.get("order") != null && StringUtils.isNotBlank(params.get("order").toString())) {
      order = params.get("order").toString();
    }
    if (params.get("sort") != null && StringUtils.isNotBlank(params.get("sort").toString())) {
      String column = normalizeSortColumn(String.valueOf(params.get("sort")));
      if (order.equalsIgnoreCase("desc")) {
        wrapper.orderByDesc(column);
      } else {
        wrapper.orderByAsc(column);
      }
    }
    return wrapper;
  }

  public static QueryWrapper sort(QueryWrapper wrapper, Map<String, Object> params) {
    List<String> orderList = new ArrayList<String>();
    List<String> sortList = new ArrayList<String>();
    if (params.get("order") != null && StringUtils.isNotBlank(params.get("order").toString())) {
      orderList = Arrays.asList(params.get("order").toString().split(","));
    }
    if (params.get("sort") != null && StringUtils.isNotBlank(params.get("sort").toString())) {
      sortList = Arrays.asList(params.get("sort").toString().split(","));
    }
    if (orderList != null && sortList != null && orderList.size() == sortList.size()) {
      for (int i = 0; i < orderList.size(); i++) {
        String column = normalizeSortColumn(sortList.get(i));
        if (orderList.get(i).equalsIgnoreCase("desc")) {
          wrapper.orderByDesc(column);
        } else {
          wrapper.orderByAsc(column);
        }
      }
    }
    return wrapper;
  }

  /**
   * 驼峰格式字符串转换为下划线格式字符串
   *
   * @param param
   * @return
   */
  public static String camelToUnderline(String param) {
    if (param == null || "".equals(param.trim())) {
      return "";
    }
    int len = param.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = param.charAt(i);
      if (Character.isUpperCase(c)) {
        sb.append(UNDERLINE);
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static void main(String[] ages) {
    System.out.println(camelToUnderline("ABCddfANM"));
  }

  public static Map camelToUnderlineMap(Map param, String pre) {

    Map<String, Object> newMap = new HashMap<String, Object>();
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String key = entry.getKey();
      String newKey = camelToUnderline(key);
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {

        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    return newMap;
  }
}
