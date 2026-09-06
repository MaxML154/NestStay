package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.HomestayInfoDao;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.view.HomestayInfoView;
import com.neststay.entity.vo.HomestayInfoVO;
import com.neststay.service.HomestayInfoService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("homestayInfoService")
public class HomestayInfoServiceImpl extends ServiceImpl<HomestayInfoDao, HomestayInfoEntity>
    implements HomestayInfoService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<HomestayInfoEntity> page =
        this.page(
            new Query<HomestayInfoEntity>(params).getPage(),
            new QueryWrapper<HomestayInfoEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper) {
    Page<HomestayInfoView> page = new Query<HomestayInfoView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<HomestayInfoVO> selectListVO(Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public HomestayInfoVO selectVO(Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<HomestayInfoView> selectListView(Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public HomestayInfoView selectView(Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }

  @Override
  public List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectTimeStatValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper) {
    return baseMapper.selectGroup(params, wrapper);
  }
}
