package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.AboutUsDao;
import com.neststay.entity.AboutUsEntity;
import com.neststay.service.AboutUsService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("aboutusService")
public class AboutUsServiceImpl extends ServiceImpl<AboutUsDao, AboutUsEntity>
    implements AboutUsService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<AboutUsEntity> pageResult =
        this.page(new Query<AboutUsEntity>(params).getPage(), new QueryWrapper<AboutUsEntity>());
    return new PageUtils(pageResult);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<AboutUsEntity> wrapper) {
    Page<AboutUsEntity> pageResult = this.page(new Query<AboutUsEntity>(params).getPage(), wrapper);
    PageUtils pageUtil = new PageUtils(pageResult);
    return pageUtil;
  }
}
