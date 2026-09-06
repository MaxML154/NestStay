package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.neststay.entity.SystemNoticeEntity;
import com.neststay.service.SystemNoticeService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/systemNotice")
public class SystemNoticeController {
  private static final List<String> CATEGORIES =
      Arrays.asList("order_refund", "stay", "audit", "account", "announce");

  @Autowired private SystemNoticeService systemNoticeService;

  @RequestMapping("/list")
  public R list(@RequestParam(required = false) String category, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    QueryWrapper<SystemNoticeEntity> wrapper =
        new QueryWrapper<SystemNoticeEntity>()
            .eq("user_id", AuthSupport.userId(request))
            .orderByDesc("id");
    if (StringUtils.isNotBlank(category) && CATEGORIES.contains(category)) {
      wrapper.eq("category", category);
    }
    return R.ok().put("data", systemNoticeService.list(wrapper));
  }

  @RequestMapping("/read/{id}")
  public R read(@PathVariable Long id, HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    SystemNoticeEntity notice = systemNoticeService.getById(id);
    if (notice == null || !AuthSupport.userId(request).equals(notice.getUserId())) {
      return R.error(404, "通知不存在");
    }
    notice.setReadFlag(1);
    systemNoticeService.updateById(notice);
    return R.ok().put("data", notice);
  }

  @RequestMapping("/readAll")
  public R readAll(HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(401, "请先登录");
    systemNoticeService.update(
        new UpdateWrapper<SystemNoticeEntity>()
            .eq("user_id", AuthSupport.userId(request))
            .eq("read_flag", 0)
            .set("read_flag", 1));
    return R.ok();
  }
}
