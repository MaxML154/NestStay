package com.neststay.service;

import com.neststay.entity.ConversationEntity;
import com.neststay.entity.ConversationMessageEntity;
import com.neststay.entity.LlmChannelEntity;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantAssistantService {
  @Autowired private LlmChannelService llmChannelService;
  @Autowired private LlmChatService llmChatService;
  @Autowired private ConversationMessageService messageService;
  @Autowired private ConversationService conversationService;

  public void maybeReply(ConversationEntity conv, String userText) {
    if (conv == null || !"merchant".equals(conv.getConvType()) || StringUtils.isBlank(userText)) {
      return;
    }
    LlmChannelEntity channel = llmChannelService.merchant(conv.getMerchantAccount());
    if (!llmChannelService.usable(channel)) return;
    String reply =
        llmChatService.complete(
            channel,
            StringUtils.defaultIfBlank(channel.getSystemPrompt(), LlmChannelService.defaultMerchantPrompt()),
            userText);
    if (StringUtils.isBlank(reply)) return;
    ConversationMessageEntity message = new ConversationMessageEntity();
    message.setConversationId(conv.getId());
    message.setSenderRole("assistant");
    message.setSenderId(0L);
    message.setContent(reply);
    message.setMessageType("text");
    messageService.save(message);
    conv.setLastMessage(reply.length() > 80 ? reply.substring(0, 80) : reply);
    conv.setLastTime(new Date());
    conversationService.updateById(conv);
  }
}
