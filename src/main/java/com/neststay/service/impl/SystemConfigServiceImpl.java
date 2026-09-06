package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SystemConfigDao;
import com.neststay.entity.SystemConfigEntity;
import com.neststay.service.SystemConfigService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 系统用户 */
@Service("configService")
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigDao, SystemConfigEntity>
    implements SystemConfigService {
  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<SystemConfigEntity> wrapper) {
    Page<SystemConfigEntity> page =
        this.page(new Query<SystemConfigEntity>(params).getPage(), wrapper);
    return new PageUtils(page);
  }
}
