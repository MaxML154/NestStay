package com.neststay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.entity.SupportTicketEntity;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface SupportTicketService extends IService<SupportTicketEntity> {
  List<String> categories();

  PageUtils pageTickets(Map<String, Object> params, HttpServletRequest request);

  R create(SupportTicketEntity body, HttpServletRequest request);

  R detail(Long id, HttpServletRequest request);

  R reply(Map<String, Object> body, HttpServletRequest request);

  R escalate(Map<String, Object> body, HttpServletRequest request);

  R resolve(Map<String, Object> body, HttpServletRequest request);

  R close(Map<String, Object> body, HttpServletRequest request);

  R reopen(Map<String, Object> body, HttpServletRequest request);

  SupportTicketEntity ensureFromRefund(RefundRequestEntity refund);
}
