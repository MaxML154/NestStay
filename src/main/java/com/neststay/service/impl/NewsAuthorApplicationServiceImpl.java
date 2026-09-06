package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.NewsAuthorApplicationDao;
import com.neststay.entity.NewsAuthorApplicationEntity;
import com.neststay.service.NewsAuthorApplicationService;
import org.springframework.stereotype.Service;

@Service
public class NewsAuthorApplicationServiceImpl
    extends ServiceImpl<NewsAuthorApplicationDao, NewsAuthorApplicationEntity>
    implements NewsAuthorApplicationService {}
