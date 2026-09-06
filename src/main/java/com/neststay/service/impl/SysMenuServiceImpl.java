package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.SysMenuDao;
import com.neststay.entity.SysMenuEntity;
import com.neststay.entity.view.SysMenuView;
import com.neststay.entity.vo.SysMenuVO;
import com.neststay.service.SysMenuService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("menuService")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity>
    implements SysMenuService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<SysMenuEntity> page =
        this.page(new Query<SysMenuEntity>(params).getPage(), new QueryWrapper<SysMenuEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<SysMenuEntity> wrapper) {
    Page<SysMenuView> page = new Query<SysMenuView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<SysMenuVO> selectListVO(Wrapper<SysMenuEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public SysMenuVO selectVO(Wrapper<SysMenuEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<SysMenuView> selectListView(Wrapper<SysMenuEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public SysMenuView selectView(Wrapper<SysMenuEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
