package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.service.ConsumerService;
import com.neststay.service.HomestayInfoService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.PlatformViewService;
import com.neststay.utils.AdminAuditSupport;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PrivacyMask;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 民宿与订单 CSV 导出。禁止导出用户全量资料。 */
@RestController
@RequestMapping("/export")
public class ExportController {
  private static final int MAX_ROWS = 5000;

  @Autowired private HomestayInfoService homestayInfoService;
  @Autowired private PlatformViewService platformViewService;
  @Autowired private HomestayRentalService homestayRentalService;
  @Autowired private ConsumerService consumerService;
  @Autowired private AdminAuditSupport adminAuditSupport;

  @GetMapping("/homestay.csv")
  public void exportHomestay(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      response.sendError(401, "请先登录");
      return;
    }
    QueryWrapper<PlatformViewEntity> platformQuery = new QueryWrapper<PlatformViewEntity>();
    QueryWrapper<HomestayInfoEntity> infoQuery = new QueryWrapper<HomestayInfoEntity>();
    if (AuthSupport.isMerchant(request)) {
      platformQuery.eq("merchant_account", AuthSupport.username(request));
    }
    List<PlatformViewEntity> platformList =
        platformViewService.list(platformQuery.last("LIMIT " + MAX_ROWS));
    List<HomestayInfoEntity> infoList =
        AuthSupport.isAdmin(request)
            ? homestayInfoService.list(infoQuery.last("LIMIT " + MAX_ROWS))
            : java.util.Collections.emptyList();

    writeCsvHeader(response, "homestay.csv");
    try (PrintWriter writer = csvWriter(response)) {
      writer.println("来源,名称,类型,价格,位置,商家账号,是否精选,审核状态");
      for (PlatformViewEntity item : platformList) {
        writer.println(
            csvRow(
                "平台民宿",
                item.getHomestayName(),
                item.getHomestayCategory(),
                item.getPricePerDay() == null ? "" : String.valueOf(item.getPricePerDay()),
                item.getHomestayLocation(),
                item.getMerchantAccount(),
                selectedText(item.getIsSelected()),
                item.getAuditStatus()));
      }
      for (HomestayInfoEntity item : infoList) {
        writer.println(
            csvRow(
                "民宿信息",
                item.getName(),
                "",
                item.getCurrentPrice() == null ? "" : String.valueOf(item.getCurrentPrice()),
                StringUtils.defaultString(item.getCity())
                    + " "
                    + StringUtils.defaultString(item.getQu()),
                "",
                selectedText(item.getIsSelected()),
                item.getAuditStatus()));
      }
    }
    adminAuditSupport.log(request, "export_csv", "homestay", "", "homestay.csv");
  }

  @GetMapping("/orders.csv")
  public void exportOrders(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      response.sendError(401, "请先登录");
      return;
    }
    QueryWrapper<HomestayRentalEntity> query = new QueryWrapper<HomestayRentalEntity>();
    if (AuthSupport.isMerchant(request)) {
      query.eq("merchant_account", AuthSupport.username(request));
    }
    List<HomestayRentalEntity> orders =
        homestayRentalService.list(query.last("LIMIT " + MAX_ROWS));
    Map<String, String> nicknames = nicknameMap();

    writeCsvHeader(response, "orders.csv");
    try (PrintWriter writer = csvWriter(response)) {
      writer.println("订单编号,房源,入住日期,退房日期,金额,状态,账号,昵称,手机");
      for (HomestayRentalEntity order : orders) {
        writer.println(
            csvRow(
                order.getOrderNumber(),
                order.getHomestayName(),
                String.valueOf(order.getCheckInDate()),
                String.valueOf(order.getCheckOutDate()),
                order.getTotalPrice() == null ? "" : String.valueOf(order.getTotalPrice()),
                StringUtils.defaultIfBlank(order.getOrderStatus(), order.getApprovalStatus()),
                order.getAccount(),
                StringUtils.defaultIfBlank(
                    nicknames.get(order.getAccount()), order.getAccount()),
                PrivacyMask.maskPhone(order.getPhone())));
      }
    }
    adminAuditSupport.log(request, "export_csv", "orders", "", "orders.csv");
  }

  private Map<String, String> nicknameMap() {
    Map<String, String> map = new HashMap<>();
    for (ConsumerEntity consumer : consumerService.list()) {
      if (consumer.getAccount() != null) {
        map.put(consumer.getAccount(), consumer.getNickname());
      }
    }
    return map;
  }

  private static String selectedText(Integer selected) {
    return selected != null && selected == 1 ? "是" : "否";
  }

  private static void writeCsvHeader(HttpServletResponse response, String filename) {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=" + filename);
  }

  private static PrintWriter csvWriter(HttpServletResponse response) throws Exception {
    OutputStreamWriter streamWriter =
        new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
    streamWriter.write('\uFEFF');
    return new PrintWriter(streamWriter);
  }

  private static String csvRow(String... values) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      if (i > 0) builder.append(',');
      builder.append(csvCell(values[i]));
    }
    return builder.toString();
  }

  private static String csvCell(String value) {
    String text = value == null || "null".equals(value) ? "" : value;
    if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
      return "\"" + text.replace("\"", "\"\"") + "\"";
    }
    return text;
  }
}
