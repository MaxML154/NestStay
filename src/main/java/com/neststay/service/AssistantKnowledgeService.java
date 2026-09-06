package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.AssistantKnowledgeDao;
import com.neststay.entity.AssistantKnowledgeEntity;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AssistantKnowledgeService
    extends ServiceImpl<AssistantKnowledgeDao, AssistantKnowledgeEntity> {

  public List<AssistantKnowledgeEntity> listEnabled() {
    return list(
        new QueryWrapper<AssistantKnowledgeEntity>()
            .eq("enabled", 1)
            .orderByAsc("sort_order")
            .orderByAsc("id"));
  }

  public AssistantKnowledgeEntity match(String ask) {
    if (StringUtils.isBlank(ask)) return null;
    String text = ask.toLowerCase();
    AssistantKnowledgeEntity best = null;
    int bestScore = 0;
    for (AssistantKnowledgeEntity item : listEnabled()) {
      int score = matchScore(text, item.getKeywords()) + matchScore(text, item.getTitle());
      if (score > bestScore) {
        bestScore = score;
        best = item;
      }
    }
    return best;
  }

  public String promptContext(int maxChars) {
    StringBuilder builder = new StringBuilder();
    for (AssistantKnowledgeEntity item : listEnabled()) {
      String block =
          "《"
              + StringUtils.defaultString(item.getTitle())
              + "》\n"
              + StringUtils.defaultString(item.getContent())
              + "\n\n";
      if (builder.length() + block.length() > Math.max(500, maxChars)) break;
      builder.append(block);
    }
    return builder.toString().trim();
  }

  private int matchScore(String text, String keywords) {
    if (StringUtils.isBlank(keywords)) return 0;
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
}
