package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.AdminUserDao;
import com.neststay.entity.AdminUserEntity;
import com.neststay.service.AdminUserService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 系统用户 */
@Service("usersService")
public class AdminUserServiceImpl extends ServiceImpl<AdminUserDao, AdminUserEntity>
    implements AdminUserService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<AdminUserEntity> page =
        this.page(
            new Query<AdminUserEntity>(params).getPage(), new QueryWrapper<AdminUserEntity>());
    return new PageUtils(page);
  }

  @Override
  public List<AdminUserEntity> selectListView(Wrapper<AdminUserEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<AdminUserEntity> wrapper) {
    Page<AdminUserEntity> page = new Query<AdminUserEntity>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }
}
