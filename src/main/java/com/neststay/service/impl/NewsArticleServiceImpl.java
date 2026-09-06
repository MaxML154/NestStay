package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.NewsArticleDao;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.view.NewsArticleView;
import com.neststay.entity.vo.NewsArticleVO;
import com.neststay.service.NewsArticleService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("newsService")
public class NewsArticleServiceImpl extends ServiceImpl<NewsArticleDao, NewsArticleEntity>
    implements NewsArticleService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<NewsArticleEntity> page =
        this.page(
            new Query<NewsArticleEntity>(params).getPage(), new QueryWrapper<NewsArticleEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<NewsArticleEntity> wrapper) {
    Page<NewsArticleView> page = new Query<NewsArticleView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<NewsArticleVO> selectListVO(Wrapper<NewsArticleEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public NewsArticleVO selectVO(Wrapper<NewsArticleEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<NewsArticleView> selectListView(Wrapper<NewsArticleEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public NewsArticleView selectView(Wrapper<NewsArticleEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
