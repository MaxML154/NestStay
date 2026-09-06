package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ConversationMessageDao;
import com.neststay.entity.ConversationMessageEntity;
import com.neststay.service.ConversationMessageService;
import org.springframework.stereotype.Service;

@Service
public class ConversationMessageServiceImpl
    extends ServiceImpl<ConversationMessageDao, ConversationMessageEntity>
    implements ConversationMessageService {}
