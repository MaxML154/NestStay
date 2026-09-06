package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.SupportTicketEntity;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.SupportTicketService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supportTicket")
public class SupportTicketController {
  @Autowired private SupportTicketService supportTicketService;
  @Autowired private HomestayRentalService rentalService;

  @RequestMapping("/categories")
  public R categories() {
    return R.ok().put("data", supportTicketService.categories());
  }

  @RequestMapping("/page")
  public R page(@RequestParam Map<String, Object> params, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    return R.ok().put("data", supportTicketService.pageTickets(params, request));
  }

  @RequestMapping("/orders")
  public R orders(HttpServletRequest request) {
    if (!AuthSupport.isConsumer(request)) return R.error(403, "仅消费者可选择关联订单");
    return R.ok()
        .put(
            "data",
            rentalService.list(
                new QueryWrapper<HomestayRentalEntity>()
                    .eq("account", AuthSupport.username(request))
                    .orderByDesc("id")
                    .last("limit 40")));
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    return supportTicketService.detail(id, request);
  }

  @RequestMapping("/create")
  public R create(@RequestBody SupportTicketEntity body, HttpServletRequest request) {
    return supportTicketService.create(body, request);
  }

  @RequestMapping("/reply")
  public R reply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    return supportTicketService.reply(body, request);
  }

  @RequestMapping("/escalate")
  public R escalate(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    return supportTicketService.escalate(body, request);
  }

  @RequestMapping("/resolve")
  public R resolve(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    return supportTicketService.resolve(body, request);
  }

  @RequestMapping("/close")
  public R close(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    return supportTicketService.close(body, request);
  }

  @RequestMapping("/reopen")
  public R reopen(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    return supportTicketService.reopen(body, request);
  }
}
