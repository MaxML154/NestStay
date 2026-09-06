package com.neststay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.neststay.config.AlipayConfig;
import com.neststay.utils.PaymentDemoHtml;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 支付宝支付服务 支持电脑网站支付（PagePay）、查询、退款 */
@Service
public class AlipayService {

  private static final Logger log = LoggerFactory.getLogger(AlipayService.class);

  private volatile AlipayClient alipayClient;

  /** 获取支付宝客户端（单例延迟初始化） */
  private AlipayClient getClient() {
    if (alipayClient == null) {
      synchronized (this) {
        if (alipayClient == null) {
          alipayClient =
              new DefaultAlipayClient(
                  AlipayConfig.URL,
                  AlipayConfig.APP_ID,
                  AlipayConfig.APP_PRIVATE_KEY,
                  AlipayConfig.FORMAT,
                  AlipayConfig.CHARSET,
                  AlipayConfig.ALIPAY_PUBLIC_KEY,
                  AlipayConfig.SIGN_TYPE);
        }
      }
    }
    return alipayClient;
  }

  /**
   * 生成支付宝页面支付表单
   *
   * @param orderNumber 订单号
   * @param totalAmount 金额
   * @param subject 订单标题
   * @param body 订单描述
   * @return HTML表单字符串
   */
  public String pagePay(String orderNumber, String totalAmount, String subject, String body) {
    try {
      AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
      request.setNotifyUrl(AlipayConfig.NOTIFY_URL);
      request.setReturnUrl(AlipayConfig.RETURN_URL);

      AlipayTradePagePayModel model = new AlipayTradePagePayModel();
      model.setOutTradeNo(orderNumber);
      model.setTotalAmount(totalAmount);
      model.setSubject(subject);
      model.setBody(body);
      model.setProductCode("FAST_INSTANT_TRADE_PAY");
      model.setTimeoutExpress("30m");

      request.setBizModel(model);
      // 返回form表单HTML
      return getClient().pageExecute(request).getBody();
    } catch (AlipayApiException e) {
      log.error("Alipay pagePay error: {}", e.getMessage(), e);
      throw new RuntimeException("支付宝支付创建失败: " + e.getMessage());
    }
  }

  /**
   * 查询支付状态
   *
   * @param orderNumber 订单号
   * @return 查询结果Map
   */
  public Map<String, String> queryOrder(String orderNumber) {
    Map<String, String> result = new HashMap<>();
    try {
      AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
      AlipayTradeQueryModel model = new AlipayTradeQueryModel();
      model.setOutTradeNo(orderNumber);
      request.setBizModel(model);

      AlipayTradeQueryResponse response = getClient().execute(request);
      result.put("code", response.getCode());
      result.put("msg", response.getMsg());
      result.put("tradeStatus", response.getTradeStatus());
      result.put("totalAmount", response.getTotalAmount());
      result.put("tradeNo", response.getTradeNo());
      result.put("buyerLogonId", response.getBuyerLogonId());

      log.info(
          "Alipay query result for {}: {} {}", orderNumber, response.getCode(), response.getMsg());
    } catch (AlipayApiException e) {
      log.error("Alipay query error: {}", e.getMessage(), e);
      result.put("code", "10003");
      result.put("msg", "查询失败: " + e.getMessage());
    }
    return result;
  }

  /**
   * 退款
   *
   * @param orderNumber 订单号
   * @param refundAmount 退款金额
   * @return 退款结果Map
   */
  public Map<String, String> refund(String orderNumber, String refundAmount) {
    Map<String, String> result = new HashMap<>();
    try {
      AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
      AlipayTradeRefundModel model = new AlipayTradeRefundModel();
      model.setOutTradeNo(orderNumber);
      model.setRefundAmount(refundAmount);
      model.setRefundReason("用户申请退款");
      request.setBizModel(model);

      AlipayTradeRefundResponse response = getClient().execute(request);
      result.put("code", response.getCode());
      result.put("msg", response.getMsg());
      result.put("refundFee", response.getRefundFee());

      log.info(
          "Alipay refund result for {}: {} {}", orderNumber, response.getCode(), response.getMsg());
    } catch (AlipayApiException e) {
      log.error("Alipay refund error: {}", e.getMessage(), e);
      result.put("code", "10003");
      result.put("msg", "退款失败: " + e.getMessage());
    }
    return result;
  }

  /** 生成模拟支付演示用的HTML 用于毕业设计演示（不需要真实支付密钥） */
  public String demoPay(String orderNumber, String amount, String subject) {
    return demoPay(orderNumber, amount, subject, "*");
  }

  public String demoPay(String orderNumber, String amount, String subject, String parentOrigin) {
    return PaymentDemoHtml.render(
        "支付宝支付 - 演示模式", "#1677ff", "支付宝", orderNumber, amount, subject, parentOrigin);
  }
}
