package com.neststay.service;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 论坛主帖 / 资讯正文自动审核。默认通过；广告、引流、明显违规直接驳回；联系方式/外链过多等送管理员。
 */
@Service
public class ContentAutoAuditService {
  public static final String PASS = "已通过";
  public static final String HOLD = "待审核";
  public static final String REJECT = "已驳回";

  private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
  private static final Pattern URL =
      Pattern.compile("(https?://|www\\.)\\S+", Pattern.CASE_INSENSITIVE);
  private static final Pattern REPEAT = Pattern.compile("(.)\\1{12,}");

  private static final String[] REJECT_WORDS = {
    "色情", "黄片", "赌博", "博彩", "赌场", "毒品", "冰毒", "大麻",
    "代开发票", "假发票", "办证", "洗钱", "刷单兼职", "日入过万"
  };
  private static final String[] HOLD_WORDS = {
    "加微信", "加薇信", "加v", "加V", "微信号", "vx号", "QQ号", "扣扣",
    "私聊成交", "代购联系", "推广返利", "兼职日结"
  };

  public Result review(String title, String content) {
    String text = plain(title) + "\n" + plain(content);
    if (StringUtils.isBlank(text)) {
      return Result.reject("内容为空");
    }
    String compact = text.replaceAll("\\s+", "");
    for (String word : REJECT_WORDS) {
      if (compact.contains(word)) {
        return Result.reject("含违规营销或违禁表述，请修改后提交");
      }
    }
    if (REPEAT.matcher(compact).find()) {
      return Result.reject("重复刷屏内容无法发布");
    }
    int phones = count(PHONE, text);
    int urls = count(URL, text);
    if (phones >= 2 || urls >= 3) {
      return Result.hold("含较多联系方式或外链，需管理员复核");
    }
    for (String word : HOLD_WORDS) {
      if (compact.toLowerCase().contains(word.toLowerCase())) {
        return Result.hold("含引流或广告嫌疑，需管理员复核");
      }
    }
    if (phones >= 1 && urls >= 1) {
      return Result.hold("同时含联系方式与外链，需管理员复核");
    }
    return Result.pass();
  }

  public static String plain(String html) {
    if (html == null) return "";
    return html.replaceAll("(?is)<script[^>]*>.*?</script>", " ")
        .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
        .replaceAll("<[^>]+>", " ")
        .replace("&nbsp;", " ")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private int count(Pattern pattern, String text) {
    int n = 0;
    java.util.regex.Matcher matcher = pattern.matcher(text);
    while (matcher.find()) n++;
    return n;
  }

  public static final class Result {
    public final String status;
    public final String reason;

    private Result(String status, String reason) {
      this.status = status;
      this.reason = reason;
    }

    public boolean passed() {
      return PASS.equals(status);
    }

    public boolean rejected() {
      return REJECT.equals(status);
    }

    public boolean hold() {
      return HOLD.equals(status);
    }

    static Result pass() {
      return new Result(PASS, "自动通过");
    }

    static Result hold(String reason) {
      return new Result(HOLD, reason);
    }

    static Result reject(String reason) {
      return new Result(REJECT, reason);
    }
  }
}
