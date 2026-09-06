package com.neststay.controller;

import com.neststay.entity.LlmChannelEntity;
import com.neststay.service.LlmChannelService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llmChannel")
public class LlmChannelController {
  @Autowired private LlmChannelService llmChannelService;

  @RequestMapping("/mine")
  public R mine(HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) {
      return R.ok().put("data", llmChannelService.publicView(llmChannelService.platformOrDefault()));
    }
    if (AuthSupport.isMerchant(request)) {
      return R.ok()
          .put(
              "data",
              llmChannelService.publicView(
                  llmChannelService.merchantOrNew(AuthSupport.username(request))));
    }
    return AuthSupport.staffError(request);
  }

  @RequestMapping("/save")
  public R save(@RequestBody LlmChannelEntity body, HttpServletRequest request) {
    if (body == null) return R.error(400, "请填写配置");
    if (AuthSupport.isAdmin(request)) {
      body.setOwnerType(LlmChannelEntity.OWNER_PLATFORM);
      body.setOwnerKey(LlmChannelEntity.OWNER_PLATFORM);
      return R.ok().put("data", llmChannelService.saveOwned(body, true));
    }
    if (AuthSupport.isMerchant(request)) {
      body.setOwnerType(LlmChannelEntity.OWNER_MERCHANT);
      body.setOwnerKey(AuthSupport.username(request));
      body.setAssistantMode(null);
      return R.ok().put("data", llmChannelService.saveOwned(body, false));
    }
    return AuthSupport.staffError(request);
  }
}
