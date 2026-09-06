package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.RecommendEventDao;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RecommendEventEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommendEventService extends ServiceImpl<RecommendEventDao, RecommendEventEntity> {
  @Autowired private PlatformViewService platformViewService;

  public void record(Long userId, String sessionId, Long listingId, String eventType, Integer dwellMs) {
    if (listingId == null || StringUtils.isBlank(eventType)) return;
    String type = eventType.trim().toLowerCase();
    RecommendEventEntity row = new RecommendEventEntity();
    row.setUserId(userId);
    row.setSessionId(StringUtils.abbreviate(StringUtils.defaultString(sessionId), 64));
    row.setListingId(listingId);
    row.setEventType(type);
    row.setDwellMs(dwellMs);
    save(row);
    if ("click".equals(type)) {
      PlatformViewEntity listing = platformViewService.getById(listingId);
      if (listing != null) {
        platformViewService.update(
            new UpdateWrapper<PlatformViewEntity>()
                .eq("id", listingId)
                .set("click_count", (listing.getClickCount() == null ? 0 : listing.getClickCount()) + 1));
      }
    }
  }
}
