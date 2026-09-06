package com.neststay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.NewsDiscussDao;
import com.neststay.entity.NewsDiscussEntity;
import com.neststay.service.NewsDiscussService;
import org.springframework.stereotype.Service;

/** 民宿资讯评论服务实现。 */
@Service
public class NewsDiscussServiceImpl extends ServiceImpl<NewsDiscussDao, NewsDiscussEntity>
    implements NewsDiscussService {}
