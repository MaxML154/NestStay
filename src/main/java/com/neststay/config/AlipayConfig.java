package com.neststay.config;

/** 支付宝支付配置类 支持沙箱环境和正式环境 修改日期：2024-08-07 */
public class AlipayConfig {

  // ===== 基础配置 =====
  /** 支付宝网关 */
  public static final String URL = "https://openapi.alipaydev.com/gateway.do";

  /** 正式环境网关: https://openapi.alipay.com/gateway.do */

  /** 应用ID（沙箱环境） */
  public static final String APP_ID = "9021000134654321";

  /** 商户私钥（PKCS8格式，去除头尾和换行） */
  public static final String APP_PRIVATE_KEY = "your_private_key_here";

  /** 支付宝公钥 */
  public static final String ALIPAY_PUBLIC_KEY = "your_alipay_public_key_here";

  /** 签名方式 RSA2 */
  public static final String SIGN_TYPE = "RSA2";

  /** 编码格式 */
  public static final String CHARSET = "UTF-8";

  /** 数据格式 */
  public static final String FORMAT = "json";

  // ===== 回调地址 =====
  /** 异步通知地址（需外网可访问） */
  public static final String NOTIFY_URL =
      "http://localhost:8080/neststay/api/payment/alipay/notify";

  /** 同步跳转地址 */
  public static final String RETURN_URL =
      "http://localhost:8080/neststay/api/payment/alipay/return";

  /** 卖家支付宝账号 */
  public static final String SELLER_ID = "neststay@business.com";
}
