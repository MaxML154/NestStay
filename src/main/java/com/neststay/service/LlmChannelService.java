package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.LlmChannelDao;
import com.neststay.entity.LlmChannelEntity;
import com.neststay.utils.EncryptUtil;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LlmChannelService extends ServiceImpl<LlmChannelDao, LlmChannelEntity> {

  public LlmChannelEntity platform() {
    return getOne(
        new QueryWrapper<LlmChannelEntity>()
            .eq("owner_type", LlmChannelEntity.OWNER_PLATFORM)
            .eq("owner_key", LlmChannelEntity.OWNER_PLATFORM)
            .last("limit 1"),
        false);
  }

  public LlmChannelEntity merchant(String account) {
    if (StringUtils.isBlank(account)) return null;
    return getOne(
        new QueryWrapper<LlmChannelEntity>()
            .eq("owner_type", LlmChannelEntity.OWNER_MERCHANT)
            .eq("owner_key", account)
            .last("limit 1"),
        false);
  }

  public LlmChannelEntity platformOrDefault() {
    LlmChannelEntity channel = platform();
    if (channel != null) return channel;
    LlmChannelEntity created = new LlmChannelEntity();
    created.setOwnerType(LlmChannelEntity.OWNER_PLATFORM);
    created.setOwnerKey(LlmChannelEntity.OWNER_PLATFORM);
    created.setEnabled(0);
    created.setProvider("openai");
    created.setBaseUrl("https://api.openai.com/v1");
    created.setModelName("gpt-4o-mini");
    created.setTemperature(new BigDecimal("0.30"));
    created.setAssistantMode(LlmChannelEntity.MODE_XIAOBO_THEN_LLM);
    created.setSystemPrompt(defaultPlatformPrompt());
    save(created);
    return created;
  }

  public LlmChannelEntity merchantOrNew(String account) {
    LlmChannelEntity channel = merchant(account);
    if (channel != null) return channel;
    LlmChannelEntity created = new LlmChannelEntity();
    created.setOwnerType(LlmChannelEntity.OWNER_MERCHANT);
    created.setOwnerKey(account);
    created.setEnabled(0);
    created.setProvider("openai");
    created.setBaseUrl("https://api.openai.com/v1");
    created.setModelName("gpt-4o-mini");
    created.setTemperature(new BigDecimal("0.30"));
    created.setSystemPrompt(defaultMerchantPrompt());
    return created;
  }

  public LlmChannelEntity saveOwned(LlmChannelEntity incoming, boolean platformOwner) {
    LlmChannelEntity existing =
        platformOwner ? platformOrDefault() : merchantOrNew(incoming.getOwnerKey());
    if (existing.getId() == null) {
      existing.setOwnerType(
          platformOwner ? LlmChannelEntity.OWNER_PLATFORM : LlmChannelEntity.OWNER_MERCHANT);
      existing.setOwnerKey(
          platformOwner ? LlmChannelEntity.OWNER_PLATFORM : incoming.getOwnerKey());
    }
    existing.setEnabled(incoming.getEnabled() != null && incoming.getEnabled() == 1 ? 1 : 0);
    existing.setProvider(StringUtils.defaultIfBlank(incoming.getProvider(), "openai"));
    existing.setBaseUrl(StringUtils.trimToNull(incoming.getBaseUrl()));
    existing.setModelName(StringUtils.trimToNull(incoming.getModelName()));
    existing.setSystemPrompt(incoming.getSystemPrompt());
    if (incoming.getTemperature() != null) existing.setTemperature(incoming.getTemperature());
    if (platformOwner) {
      String mode = StringUtils.defaultIfBlank(incoming.getAssistantMode(), LlmChannelEntity.MODE_XIAOBO_THEN_LLM);
      if (!LlmChannelEntity.MODE_XIAOBO_ONLY.equals(mode)
          && !LlmChannelEntity.MODE_LLM_ONLY.equals(mode)
          && !LlmChannelEntity.MODE_XIAOBO_THEN_LLM.equals(mode)) {
        mode = LlmChannelEntity.MODE_XIAOBO_THEN_LLM;
      }
      existing.setAssistantMode(mode);
    } else {
      existing.setAssistantMode(null);
    }
    if (StringUtils.isNotBlank(incoming.getApiKey())) {
      existing.setApiKeyEnc(EncryptUtil.aesEncrypt(incoming.getApiKey().trim()));
    }
    saveOrUpdate(existing);
    return publicView(existing);
  }

  public LlmChannelEntity publicView(LlmChannelEntity channel) {
    if (channel == null) return null;
    LlmChannelEntity view = new LlmChannelEntity();
    view.setId(channel.getId());
    view.setCreateTime(channel.getCreateTime());
    view.setOwnerType(channel.getOwnerType());
    view.setOwnerKey(channel.getOwnerKey());
    view.setEnabled(channel.getEnabled());
    view.setProvider(channel.getProvider());
    view.setBaseUrl(channel.getBaseUrl());
    view.setModelName(channel.getModelName());
    view.setSystemPrompt(channel.getSystemPrompt());
    view.setTemperature(channel.getTemperature());
    view.setAssistantMode(channel.getAssistantMode());
    view.setApiKey(null);
    view.setApiKeyEnc(null);
    view.setApiKeySet(StringUtils.isNotBlank(channel.getApiKeyEnc()));
    return view;
  }

  public boolean usable(LlmChannelEntity channel) {
    if (channel == null
        || !Integer.valueOf(1).equals(channel.getEnabled())
        || StringUtils.isBlank(channel.getModelName())) {
      return false;
    }
    if (StringUtils.isNotBlank(channel.getApiKeyEnc())) return true;
    String base = StringUtils.defaultString(channel.getBaseUrl()).toLowerCase();
    return base.contains("localhost") || base.contains("127.0.0.1");
  }

  public String decryptKey(LlmChannelEntity channel) {
    if (channel == null || StringUtils.isBlank(channel.getApiKeyEnc())) return null;
    return EncryptUtil.aesDecrypt(channel.getApiKeyEnc());
  }

  public static String defaultPlatformPrompt() {
    return "你是 NestStay 民宿平台的内部客服模型，不是真人管理员。消费者不能选择模型：小搏先答，知识库未命中才问你。用户输入「转人工」后才由管理员回复，调用失败不要自动转人工。不要编造订单、退款或支付结果。优先依据知识库摘录作答。";
  }

  public static String defaultMerchantPrompt() {
    return "你是该民宿商家的客服助手，用简短中文回答房源、入住和价格问题。不知道的请让用户等待商家本人回复，不要假装已转人工，不要编造订单。";
  }
}
