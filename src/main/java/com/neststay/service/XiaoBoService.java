package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.AssistantKnowledgeEntity;
import com.neststay.entity.ChatHelperEntity;
import com.neststay.entity.ConversationEntity;
import com.neststay.entity.LlmChannelEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 平台智能助手小搏：关键词 + 知识库。消费者不能选择模型。 默认先小搏，知识库未命中再走平台模型；仅用户明确转人工时才进管理员。
 * 调用模型失败时退回原文案，不自动转人工。
 */
@Service
public class XiaoBoService {
  public static final String WELCOME =
      "你好，我是 NestStay 智能助手小搏。预订、退款、入住都可以先问我。知识库没有的问题会交给平台模型补充。需要管理员时请输入「转人工」。";
  public static final String FALLBACK = "小搏还没在知识库里找到答案。可以换个说法，或输入「转人工」联系管理员。";

  private final ConcurrentHashMap<Long, Integer> roundsByUser = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, Boolean> humanRequested = new ConcurrentHashMap<>();

  @Autowired private ChatHelperService chatHelperService;
  @Autowired private AssistantKnowledgeService assistantKnowledgeService;
  @Autowired private LlmChannelService llmChannelService;
  @Autowired private LlmChatService llmChatService;
  @Autowired private ConversationService conversationService;

  public int rounds(HttpServletRequest request) {
    Long userId = com.neststay.utils.AuthSupport.userId(request);
    if (userId == null) return 0;
    return roundsByUser.getOrDefault(userId, 0);
  }

  public Map<String, Object> ask(String question, HttpServletRequest request) {
    String ask = StringUtils.trimToEmpty(question);
    int before = rounds(request);
    Map<String, Object> matched = answer(ask, request);
    int after = before + 1;
    Long userId = com.neststay.utils.AuthSupport.userId(request);
    if (userId != null) roundsByUser.put(userId, after);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("ask", ask);
    data.put("reply", matched.get("reply"));
    data.put("source", matched.get("source"));
    data.put("transfer", Boolean.TRUE.equals(matched.get("transfer")));
    data.put("rounds", after);
    data.put("canTransfer", canTransfer(request));
    data.put("remain", 0);
    return data;
  }

  public Map<String, Object> status(HttpServletRequest request) {
    int rounds = rounds(request);
    LlmChannelEntity channel = llmChannelService.platform();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("welcome", WELCOME);
    data.put("rounds", rounds);
    data.put("canTransfer", canTransfer(request));
    data.put("remain", 0);
    if (Boolean.TRUE.equals(data.get("canTransfer"))) {
      ConversationEntity conv = existingPlatformConversation(request);
      if (conv != null) data.put("conversation", conv);
    }
    data.put(
        "assistantMode",
        channel == null
            ? LlmChannelEntity.MODE_XIAOBO_THEN_LLM
            : StringUtils.defaultIfBlank(
                channel.getAssistantMode(), LlmChannelEntity.MODE_XIAOBO_THEN_LLM));
    data.put("llmEnabled", llmChannelService.usable(channel));
    return data;
  }

  public String transferDeniedMessage(HttpServletRequest request) {
    return "请输入「转人工」联系管理员";
  }

  public boolean canTransfer(HttpServletRequest request) {
    Long userId = com.neststay.utils.AuthSupport.userId(request);
    if (userId == null || !com.neststay.utils.AuthSupport.isConsumer(request)) return false;
    if (Boolean.TRUE.equals(humanRequested.get(userId))) return true;
    ConversationEntity conv = existingPlatformConversation(request);
    if (conv != null) {
      humanRequested.put(userId, true);
      return true;
    }
    return false;
  }

  private ConversationEntity existingPlatformConversation(HttpServletRequest request) {
    Long userId = com.neststay.utils.AuthSupport.userId(request);
    if (userId == null || conversationService == null) return null;
    return conversationService.getOne(
        new QueryWrapper<ConversationEntity>()
            .eq("consumer_id", userId)
            .eq("conv_type", "platform")
            .and(w -> w.eq("deleted_by_consumer", 0).or().isNull("deleted_by_consumer"))
            .last("limit 1"),
        false);
  }

  private Map<String, Object> answer(String ask, HttpServletRequest request) {
    Map<String, Object> data = new LinkedHashMap<>();
    if (StringUtils.isBlank(ask)) {
      data.put("reply", FALLBACK);
      data.put("source", "fallback");
      return data;
    }
    if (isHumanKeyword(ask)) {
      Long userId = com.neststay.utils.AuthSupport.userId(request);
      if (userId != null) humanRequested.put(userId, true);
      data.put("reply", "已为你转接平台管理员，请继续说明问题。");
      data.put("source", "xiaobo");
      data.put("transfer", true);
      return data;
    }

    LlmChannelEntity channel = llmChannelService.platform();
    String mode =
        channel == null
            ? LlmChannelEntity.MODE_XIAOBO_THEN_LLM
            : StringUtils.defaultIfBlank(
                channel.getAssistantMode(), LlmChannelEntity.MODE_XIAOBO_THEN_LLM);

    if (LlmChannelEntity.MODE_LLM_ONLY.equals(mode)) {
      String llm = callPlatformModel(channel, ask);
      if (StringUtils.isNotBlank(llm)) {
        data.put("reply", llm);
        data.put("source", "llm");
        return data;
      }
      data.put("reply", FALLBACK);
      data.put("source", "fallback");
      return data;
    }

    String local = matchLocal(ask);
    if (local != null) {
      data.put("reply", local);
      data.put("source", "xiaobo");
      return data;
    }

    if (LlmChannelEntity.MODE_XIAOBO_THEN_LLM.equals(mode)) {
      String llm = callPlatformModel(channel, ask);
      if (StringUtils.isNotBlank(llm)) {
        data.put("reply", llm);
        data.put("source", "llm");
        return data;
      }
    }

    data.put("reply", FALLBACK);
    data.put("source", "fallback");
    return data;
  }

  private String callPlatformModel(LlmChannelEntity channel, String ask) {
    if (!llmChannelService.usable(channel)) return null;
    String system =
        StringUtils.defaultIfBlank(
            channel.getSystemPrompt(), LlmChannelService.defaultPlatformPrompt());
    String context = assistantKnowledgeService.promptContext(3500);
    if (StringUtils.isNotBlank(context)) {
      system = system + "\n\n【知识库摘录】\n" + context;
    }
    return llmChatService.complete(channel, system, ask);
  }

  private String matchLocal(String ask) {
    AssistantKnowledgeEntity knowledge = assistantKnowledgeService.match(ask);
    if (knowledge != null && StringUtils.isNotBlank(knowledge.getContent())) {
      return knowledge.getContent();
    }
    List<ChatHelperEntity> helpers =
        chatHelperService.list(new QueryWrapper<ChatHelperEntity>().orderByAsc("id"));
    if (helpers == null) helpers = new ArrayList<>();
    ChatHelperEntity bestHelper = null;
    int bestHelperScore = 0;
    for (ChatHelperEntity helper : helpers) {
      if (helper == null || isHumanKeyword(helper.getAsk())) continue;
      int score = keywordScore(ask, helper.getAsk());
      if (score > bestHelperScore) {
        bestHelperScore = score;
        bestHelper = helper;
      }
    }
    if (bestHelper != null) {
      return StringUtils.defaultIfBlank(bestHelper.getReply(), FALLBACK);
    }
    String bestRule = null;
    int bestRuleScore = 0;
    for (Map.Entry<String, String> entry : fallbackRules().entrySet()) {
      int score = keywordScore(ask, entry.getKey());
      if (score > bestRuleScore) {
        bestRuleScore = score;
        bestRule = entry.getValue();
      }
    }
    return bestRule;
  }

  private LinkedHashMap<String, String> fallbackRules() {
    LinkedHashMap<String, String> rules = new LinkedHashMap<>();
    rules.put(
        "退款,申请退款,怎么退,如何退",
        "退款请到「订单」里找到对应订单提出申请。若商家驳回，可在同一订单页申请平台介入。房源不符、入住体验、评价等问题请到「申述工单」提交，商家 48 小时未处理可转交平台。");
    rules.put(
        "预订,预定,订房,下单,怎么订,如何订,如何预订,怎么预订,booking",
        "预订路径：打开「全部民宿」选择房源 → 填写入住/退房日期和人数 → 提交订单。商家审核通过后，请在 2 小时内到「订单」完成演示支付，超时将自动取消预订。支付成功即进入待入住。");
    rules.put(
        "工单,申述,申诉,投诉",
        "打开个人主页「申述」或顶栏菜单「申述工单」提交。可选择退款争议、房源不符、入住体验、评价争议、支付问题、账号隐私等类型，并上传凭证。");
    rules.put("入住,退房,客房", "入住确认、退房和客房服务请到个人主页「居住状况」查看当前订单。");
    rules.put("密码,账号,实名", "账号、密码和实名信息请到个人主页 → 个人信息 中修改。修改姓名需要手机验证码。");
    rules.put("发帖,论坛", "测试期已对登录用户开放论坛发帖。请到社区论坛发布帖子，主题帖仍需管理员审核后展示。");
    rules.put("资讯,作者", "发布资讯请先打开「我的主页」→「发布」→「资讯」，在那里申请作者或发表文章。");
    rules.put("支付,支付宝", "当前为演示支付。商家审核通过后请在 2 小时内支付，超时预订会自动取消。可在订单页查看倒计时与支付结果。");
    return rules;
  }

  private int keywordScore(String ask, String keywords) {
    if (StringUtils.isBlank(keywords) || StringUtils.isBlank(ask)) return 0;
    String text = ask.toLowerCase();
    int score = 0;
    for (String raw : keywords.split("[,/，]")) {
      String word = raw.trim();
      if (word.isEmpty()) continue;
      String needle = word.toLowerCase();
      if (!text.contains(needle)) continue;
      score += needle.length();
      if (text.contains("退款") && needle.contains("退款")) {
        score += 100;
      }
    }
    return score;
  }

  private boolean containsAny(String ask, String keywords) {
    return keywordScore(ask, keywords) > 0;
  }

  private boolean isHumanKeyword(String text) {
    return containsAny(StringUtils.defaultString(text), "转人工,找人工,转接人工,人工客服");
  }
}
