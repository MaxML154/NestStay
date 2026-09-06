package com.neststay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neststay.entity.LlmChannelEntity;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LlmChatService {
  private static final Logger log = LoggerFactory.getLogger(LlmChatService.class);
  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

  @Autowired private LlmChannelService llmChannelService;
  @Autowired private ObjectMapper objectMapper;

  public String complete(LlmChannelEntity channel, String systemPrompt, String userMessage) {
    if (!llmChannelService.usable(channel) || StringUtils.isBlank(userMessage)) return null;
    String key = StringUtils.defaultIfBlank(llmChannelService.decryptKey(channel), "lm-studio");
    try {
      String url = chatUrl(channel.getBaseUrl());
      boolean lmStudioChat = isLmStudioChatApi(url);
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("model", channel.getModelName());
      if (lmStudioChat) {
        if (StringUtils.isNotBlank(systemPrompt)) body.put("system_prompt", systemPrompt);
        body.put("input", userMessage);
      } else {
        body.put(
            "temperature",
            channel.getTemperature() == null ? 0.3 : channel.getTemperature().doubleValue());
        List<Map<String, String>> messages = new ArrayList<>();
        if (StringUtils.isNotBlank(systemPrompt)) {
          messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);
      }
      String json = objectMapper.writeValueAsString(body);
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(url))
              .timeout(Duration.ofSeconds(90))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + key)
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        log.warn("llm chat failed status={} body={}", response.statusCode(), trim(response.body()));
        return null;
      }
      return extractText(objectMapper.readTree(response.body()));
    } catch (Exception exception) {
      log.warn("llm chat error: {}", exception.getMessage());
      return null;
    }
  }

  static boolean isLmStudioChatApi(String url) {
    String raw = StringUtils.defaultString(url).toLowerCase();
    return raw.contains("/api/v1/chat") && !raw.contains("/chat/completions");
  }

  private String extractText(JsonNode root) {
    if (root == null) return null;
    JsonNode openai = root.path("choices").path(0).path("message").path("content");
    if (!openai.isMissingNode() && !openai.isNull() && StringUtils.isNotBlank(openai.asText())) {
      return openai.asText().trim();
    }
    JsonNode output = root.path("output");
    if (output.isArray()) {
      for (JsonNode item : output) {
        JsonNode content = item.path("content");
        if (content.isTextual() && StringUtils.isNotBlank(content.asText())) {
          return content.asText().trim();
        }
        if (content.isArray()) {
          for (JsonNode part : content) {
            if (part.isTextual() && StringUtils.isNotBlank(part.asText())) return part.asText().trim();
            JsonNode nested = part.path("text");
            if (nested.isTextual() && StringUtils.isNotBlank(nested.asText())) {
              return nested.asText().trim();
            }
          }
        }
      }
    }
    JsonNode outputText = root.path("output_text");
    if (outputText.isTextual()) return StringUtils.trimToNull(outputText.asText());
    return null;
  }

  private String chatUrl(String baseUrl) {
    String raw = StringUtils.defaultIfBlank(baseUrl, "https://api.openai.com/v1").trim();
    while (raw.endsWith("/")) raw = raw.substring(0, raw.length() - 1);
    if (raw.endsWith("/chat/completions")) return raw;
    if (isLmStudioChatApi(raw) || raw.endsWith("/api/v1/chat")) return raw;
    if (raw.endsWith("/v1")) return raw + "/chat/completions";
    return raw + "/v1/chat/completions";
  }

  private String trim(String text) {
    if (text == null) return "";
    return text.length() > 180 ? text.substring(0, 180) : text;
  }
}
