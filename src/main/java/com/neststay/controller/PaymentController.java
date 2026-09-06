package com.neststay.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.config.AlipayConfig;
import com.neststay.service.AlipayService;
import com.neststay.utils.PaymentDemoHtml;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** NestStay 统一支付控制器 支持支付宝、银联云闪付两种支付方式 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

  @Autowired private AlipayService alipayService;

  private static final Set<String> DEMO_PAID = ConcurrentHashMap.newKeySet();

  // ==================== 支付宝支付 ====================

  /**
   * 发起支付宝网页支付（演示模式，无需真实密钥） GET /api/payment/alipay/pay?orderNumber=xxx&amount=99.00&subject=民宿预订
   */
  @IgnoreAuth
  @GetMapping("/alipay/pay")
  public void alipayPay(
      @RequestParam String orderNumber,
      @RequestParam String amount,
      @RequestParam(defaultValue = "NestStay民宿预订") String subject,
      @RequestParam(required = false) String parentOrigin,
      HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    String html = alipayService.demoPay(orderNumber, amount, subject, parentOrigin);
    response.getWriter().write(html);
    response.getWriter().flush();
  }

  /** 支付宝异步通知接收 POST /api/payment/alipay/notify */
  @IgnoreAuth
  @PostMapping("/alipay/notify")
  public String alipayNotify(HttpServletRequest request) {
    try {
      Map<String, String> params = new HashMap<>();
      Map<String, String[]> requestParams = request.getParameterMap();
      for (String name : requestParams.keySet()) {
        String[] values = requestParams.get(name);
        String valueStr = "";
        for (int i = 0; i < values.length; i++) {
          valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
        }
        params.put(name, valueStr);
      }

      // 验证签名
      boolean signVerified =
          AlipaySignature.rsaCheckV1(
              params, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);

      if (signVerified) {
        // 商户订单号
        String outTradeNo = params.get("out_trade_no");
        // 支付宝交易号
        String tradeNo = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");

        log.info(
            "支付宝异步通知验证通过: outTradeNo={}, tradeNo={}, status={}", outTradeNo, tradeNo, tradeStatus);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
          // TODO: 更新订单支付状态为"已支付"
          // orderService.updatePaymentStatus(outTradeNo, "已支付", tradeNo);
          log.info("订单 {} 支付成功", outTradeNo);
        }
        return "success";
      } else {
        log.warn("支付宝异步通知签名验证失败");
        return "fail";
      }
    } catch (Exception e) {
      log.error("支付宝异步通知处理异常: {}", e.getMessage(), e);
      return "fail";
    }
  }

  /** 支付宝同步跳转 GET /api/payment/alipay/return */
  @IgnoreAuth
  @GetMapping("/alipay/return")
  public String alipayReturn(HttpServletRequest request) {
    try {
      Map<String, String> params = new HashMap<>();
      Map<String, String[]> requestParams = request.getParameterMap();
      for (String name : requestParams.keySet()) {
        String[] values = requestParams.get(name);
        String valueStr = "";
        for (int i = 0; i < values.length; i++) {
          valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
        }
        params.put(name, valueStr);
      }

      boolean signVerified =
          AlipaySignature.rsaCheckV1(
              params, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);

      if (signVerified) {
        return "<html><body><script>alert('支付成功！');window.location.href='/neststay/front/front/dist/index.html';</script></body></html>";
      } else {
        return "<html><body><script>alert('签名验证失败');window.location.href='/neststay/front/front/dist/index.html';</script></body></html>";
      }
    } catch (Exception e) {
      return "<html><body><script>alert('支付异常:"
          + e.getMessage()
          + "');window.location.href='/neststay/front/front/dist/index.html';</script></body></html>";
    }
  }

  /** 查询支付状态 GET /api/payment/alipay/query?orderNumber=xxx */
  @IgnoreAuth
  @GetMapping("/alipay/query")
  public R queryAlipayOrder(@RequestParam String orderNumber) {
    Map<String, String> result = alipayService.queryOrder(orderNumber);
    return R.ok().put("data", result);
  }

  // ==================== 银联云闪付 ====================

  /** 发起银联云闪付支付（演示模式） GET /api/payment/unionpay/pay?orderNumber=xxx&amount=99.00&subject=民宿预订 */
  @IgnoreAuth
  @GetMapping("/unionpay/pay")
  public void unionpayPay(
      @RequestParam String orderNumber,
      @RequestParam String amount,
      @RequestParam(defaultValue = "NestStay民宿预订") String subject,
      @RequestParam(required = false) String parentOrigin,
      HttpServletResponse response)
      throws IOException {
    writeDemo(
        response,
        PaymentDemoHtml.render(
            "云闪付支付 - 演示模式", "#e60012", "银联云闪付", orderNumber, amount, subject, parentOrigin));
  }

  /** 微信/银行等其余演示渠道统一开窗。 */
  @IgnoreAuth
  @GetMapping("/demo/pay")
  public void demoPay(
      @RequestParam String orderNumber,
      @RequestParam String amount,
      @RequestParam(defaultValue = "NestStay民宿预订") String subject,
      @RequestParam(defaultValue = "wechat") String channel,
      @RequestParam(required = false) String parentOrigin,
      HttpServletResponse response)
      throws IOException {
    String[] style = demoStyle(channel);
    writeDemo(
        response,
        PaymentDemoHtml.render(
            style[0], style[1], style[2], orderNumber, amount, subject, parentOrigin));
  }

  /** 演示支付完成：支付弹窗点击确认后登记，父页轮询后把订单标为已支付。 */
  @IgnoreAuth
  @GetMapping("/demo/complete")
  public R demoComplete(@RequestParam String orderNumber) {
    if (orderNumber == null || orderNumber.trim().isEmpty()) {
      return R.error(400, "缺少订单号");
    }
    DEMO_PAID.add(orderNumber.trim());
    return R.ok();
  }

  @IgnoreAuth
  @GetMapping("/demo/status")
  public R demoStatus(@RequestParam String orderNumber) {
    Map<String, Object> data = new HashMap<>();
    boolean paid =
        orderNumber != null && !orderNumber.trim().isEmpty() && DEMO_PAID.contains(orderNumber.trim());
    data.put("paid", paid);
    return R.ok().put("data", data);
  }

  /** 银联云闪付异步通知 */
  @IgnoreAuth
  @PostMapping("/unionpay/notify")
  public String unionpayNotify(HttpServletRequest request) {
    String orderNumber = request.getParameter("orderId");
    String respCode = request.getParameter("respCode");
    String respMsg = request.getParameter("respMsg");

    log.info("银联云闪付通知: orderNumber={}, respCode={}, respMsg={}", orderNumber, respCode, respMsg);

    if ("00".equals(respCode)) {
      // TODO: 更新订单支付状态
      // orderService.updatePaymentStatus(orderNumber, "已支付", null);
      log.info("云闪付订单 {} 支付成功", orderNumber);
    }
    return "success";
  }

  private void writeDemo(HttpServletResponse response, String html) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(html);
    response.getWriter().flush();
  }

  private String[] demoStyle(String channel) {
    if ("unionpay".equals(channel)) {
      return new String[] {"云闪付支付 - 演示模式", "#e60012", "银联云闪付"};
    }
    if ("alipay".equals(channel)) {
      return new String[] {"支付宝支付 - 演示模式", "#1677ff", "支付宝"};
    }
    if ("ccb".equals(channel)) {
      return new String[] {"建设银行支付 - 演示模式", "#003a8c", "建设银行"};
    }
    if ("abc".equals(channel)) {
      return new String[] {"农业银行支付 - 演示模式", "#319c8b", "农业银行"};
    }
    if ("boc".equals(channel)) {
      return new String[] {"中国银行支付 - 演示模式", "#a71e32", "中国银行"};
    }
    if ("bocom".equals(channel)) {
      return new String[] {"交通银行支付 - 演示模式", "#1a3c8c", "交通银行"};
    }
    return new String[] {"微信支付 - 演示模式", "#07c160", "微信支付"};
  }
}
