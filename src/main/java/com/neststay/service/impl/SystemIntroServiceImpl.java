package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SystemIntroDao;
import com.neststay.entity.SystemIntroEntity;
import com.neststay.service.SystemIntroService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("systemintroService")
public class SystemIntroServiceImpl extends ServiceImpl<SystemIntroDao, SystemIntroEntity>
    implements SystemIntroService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<SystemIntroEntity> pageResult =
        this.page(
            new Query<SystemIntroEntity>(params).getPage(), new QueryWrapper<SystemIntroEntity>());
    return new PageUtils(pageResult);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<SystemIntroEntity> wrapper) {
    Page<SystemIntroEntity> pageResult =
        this.page(new Query<SystemIntroEntity>(params).getPage(), wrapper);
    PageUtils pageUtil = new PageUtils(pageResult);
    return pageUtil;
  }
}
