package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.PlatformViewDao;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.PlatformViewView;
import com.neststay.entity.vo.PlatformViewVO;
import com.neststay.service.PlatformViewService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("platformViewService")
public class PlatformViewServiceImpl extends ServiceImpl<PlatformViewDao, PlatformViewEntity>
    implements PlatformViewService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<PlatformViewEntity> page =
        this.page(
            new Query<PlatformViewEntity>(params).getPage(),
            new QueryWrapper<PlatformViewEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper) {
    Page<PlatformViewView> page = new Query<PlatformViewView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<PlatformViewVO> selectListVO(Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public PlatformViewVO selectVO(Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<PlatformViewView> selectListView(Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public PlatformViewView selectView(Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }

  @Override
  public List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectTimeStatValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper) {
    return baseMapper.selectGroup(params, wrapper);
  }
}
