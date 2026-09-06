package com.neststay.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** 演示支付页 HTML，仅用于毕业设计演示，不依赖真实支付密钥。 */
public final class PaymentDemoHtml {
  private PaymentDemoHtml() {}

  public static String render(
      String title,
      String brandColor,
      String methodLabel,
      String orderNumber,
      String amount,
      String subject) {
    return render(title, brandColor, methodLabel, orderNumber, amount, subject, "*");
  }

  public static String render(
      String title,
      String brandColor,
      String methodLabel,
      String orderNumber,
      String amount,
      String subject,
      String parentOrigin) {
    String orderJs = escapeJs(orderNumber);
    String originJs = escapeJs(resolveParentOrigin(parentOrigin));
    String color = escapeHtml(brandColor);
    return "<!DOCTYPE html>\n<html>\n<head>\n"
        + "<meta charset=\"UTF-8\">\n"
        + "<title>"
        + escapeHtml(title)
        + "</title>\n"
        + "<style>\n"
        + "body{font-family:'Microsoft YaHei',sans-serif;text-align:center;padding-top:48px;background:#f5f5f5;margin:0;}\n"
        + ".pay-card{width:420px;margin:0 auto;background:#fff;border-radius:12px;padding:36px 40px;box-shadow:0 4px 20px rgba(0,0,0,0.1);}\n"
        + ".logo-wrap{margin:0 auto 16px;display:flex;align-items:center;justify-content:center;min-height:72px;}\n"
        + ".logo-wrap img{max-width:220px;max-height:72px;object-fit:contain;}\n"
        + ".pay-card h2{color:"
        + color
        + ";margin:0 0 20px;font-size:20px;}\n"
        + ".amount{font-size:32px;color:#ff4d4f;font-weight:bold;margin:20px 0;}\n"
        + ".info{color:#666;margin:10px 0;font-size:14px;}\n"
        + ".btn{display:inline-block;width:100%;padding:14px;background:"
        + color
        + ";color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;margin-top:20px;}\n"
        + ".btn[disabled]{opacity:.7;cursor:default;}\n"
        + ".divider{color:#999;margin:20px 0;}\n"
        + ".ok{color:#0F672D;font-size:18px;font-weight:700;}\n"
        + "</style>\n</head>\n<body>\n"
        + "<div class=\"pay-card\" id=\"card\">\n"
        + "<div class=\"logo-wrap\">"
        + logoHtml(methodLabel)
        + "</div>\n"
        + "<h2>确认支付</h2>\n"
        + "<p class=\"info\">订单号: "
        + escapeHtml(orderNumber)
        + "</p>\n"
        + "<p class=\"info\">商品: "
        + escapeHtml(subject)
        + "</p>\n"
        + "<div class=\"amount\">¥ "
        + escapeHtml(amount)
        + "</div>\n"
        + "<p class=\"divider\">—— 支付方式："
        + escapeHtml(methodLabel)
        + " ——</p>\n"
        + "<button class=\"btn\" id=\"payBtn\" onclick=\"handlePay()\">确认支付 ¥ "
        + escapeHtml(amount)
        + "</button>\n"
        + "<br/><br/><a href=\"javascript:handleCancel()\" style=\"color:#999\">取消支付</a>\n"
        + "</div>\n"
        + "<script>\n"
        + "var orderNumber = '"
        + orderJs
        + "';\n"
        + "var parentOrigin = '"
        + originJs
        + "';\n"
        + "var done = false;\n"
        + "function notify(status) {\n"
        + "  try {\n"
        + "    if (window.opener) {\n"
        + "      window.opener.postMessage({\n"
        + "        type: 'NESTSTAY_PAY_RESULT',\n"
        + "        status: status,\n"
        + "        orderNumber: orderNumber\n"
        + "      }, parentOrigin || '*');\n"
        + "    }\n"
        + "  } catch (e) {\n"
        + "    try {\n"
        + "      if (window.opener) {\n"
        + "        window.opener.postMessage({\n"
        + "          type: 'NESTSTAY_PAY_RESULT',\n"
        + "          status: status,\n"
        + "          orderNumber: orderNumber\n"
        + "        }, '*');\n"
        + "      }\n"
        + "    } catch (ignore) {}\n"
        + "  }\n"
        + "}\n"
        + "function showPaid() {\n"
        + "  var card = document.getElementById('card');\n"
        + "  if (!card) return;\n"
        + "  card.innerHTML = '<p class=\"ok\">支付成功（演示模式）</p><p class=\"info\">本窗口将自动关闭</p>';\n"
        + "}\n"
        + "function handlePay() {\n"
        + "  if (done) return;\n"
        + "  done = true;\n"
        + "  var btn = document.getElementById('payBtn');\n"
        + "  if (btn) { btn.disabled = true; btn.innerText = '支付中...'; }\n"
        + "  var url = '/neststay/api/payment/demo/complete?orderNumber=' + encodeURIComponent(orderNumber);\n"
        + "  fetch(url, { credentials: 'include' }).catch(function(){}).then(function() {\n"
        + "    notify('success');\n"
        + "    showPaid();\n"
        + "    setTimeout(function(){ try { window.close(); } catch (e) {} }, 800);\n"
        + "  });\n"
        + "}\n"
        + "function handleCancel() {\n"
        + "  notify('cancel');\n"
        + "  try { window.close(); } catch (e) {}\n"
        + "}\n"
        + "</script>\n</body>\n</html>";
  }

  static String resolveParentOrigin(String parentOrigin) {
    if (parentOrigin == null) return "*";
    String raw = parentOrigin.trim();
    if (raw.isEmpty() || "*".equals(raw)) return "*";
    try {
      int guard = 0;
      while (guard++ < 3 && raw.contains("%")) {
        String decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        if (decoded.equals(raw)) break;
        raw = decoded.trim();
      }
    } catch (Exception ignored) {
      return "*";
    }
    if ("*".equals(raw)) return "*";
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw;
    return "*";
  }

  private static String logoHtml(String methodLabel) {
    String file = logoFile(methodLabel);
    String label = escapeHtml(methodLabel);
    return "<img src=\"/neststay/pay-logos/"
        + file
        + "\" alt=\""
        + label
        + "\" onerror=\"this.style.display='none';this.insertAdjacentHTML('afterend','<strong>"
        + label
        + "</strong>')\">";
  }

  private static String logoFile(String methodLabel) {
    if (methodLabel == null) return "zhifubao.svg";
    if (methodLabel.contains("云闪付")) return "yunshanfu.svg";
    if (methodLabel.contains("微信")) return "weixin.svg";
    if (methodLabel.contains("建设")) return "jianshe.svg";
    if (methodLabel.contains("农业")) return "nongye.svg";
    if (methodLabel.contains("中国银行")) return "zhongguo.svg";
    if (methodLabel.contains("交通")) return "jiaotong.svg";
    return "zhifubao.svg";
  }

  private static String escapeHtml(String value) {
    if (value == null) return "";
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private static String escapeJs(String value) {
    if (value == null) return "";
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("</", "<\\/");
  }
}
