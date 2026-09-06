package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.StayServiceDao;
import com.neststay.entity.StayServiceEntity;
import com.neststay.service.StayServiceCatalogService;
import org.springframework.stereotype.Service;

@Service
public class StayServiceCatalogServiceImpl extends ServiceImpl<StayServiceDao, StayServiceEntity>
    implements StayServiceCatalogService {}
