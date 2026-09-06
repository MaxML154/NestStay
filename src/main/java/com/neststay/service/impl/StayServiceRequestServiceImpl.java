package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.StayServiceRequestDao;
import com.neststay.entity.StayServiceRequestEntity;
import com.neststay.service.StayServiceRequestService;
import org.springframework.stereotype.Service;

@Service
public class StayServiceRequestServiceImpl
    extends ServiceImpl<StayServiceRequestDao, StayServiceRequestEntity>
    implements StayServiceRequestService {}
