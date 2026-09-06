package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ConversationDao;
import com.neststay.entity.ConversationEntity;
import com.neststay.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationDao, ConversationEntity>
    implements ConversationService {}
