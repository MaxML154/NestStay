package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.RefundRequestDao;
import com.neststay.entity.RefundRequestEntity;
import com.neststay.service.RefundRequestService;
import org.springframework.stereotype.Service;

@Service
public class RefundRequestServiceImpl extends ServiceImpl<RefundRequestDao, RefundRequestEntity>
    implements RefundRequestService {}
