package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SupportTicketMessageDao;
import com.neststay.entity.SupportTicketMessageEntity;
import com.neststay.service.SupportTicketMessageService;
import org.springframework.stereotype.Service;

@Service
public class SupportTicketMessageServiceImpl
    extends ServiceImpl<SupportTicketMessageDao, SupportTicketMessageEntity>
    implements SupportTicketMessageService {}
