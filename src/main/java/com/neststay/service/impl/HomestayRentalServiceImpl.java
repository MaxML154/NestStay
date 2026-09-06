package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.HomestayRentalDao;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.view.HomestayRentalView;
import com.neststay.entity.vo.HomestayRentalVO;
import com.neststay.service.HomestayRentalService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("homestayRentalService")
public class HomestayRentalServiceImpl extends ServiceImpl<HomestayRentalDao, HomestayRentalEntity>
    implements HomestayRentalService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<HomestayRentalEntity> page =
        this.page(
            new Query<HomestayRentalEntity>(params).getPage(),
            new QueryWrapper<HomestayRentalEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper) {
    Page<HomestayRentalView> page = new Query<HomestayRentalView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<HomestayRentalVO> selectListVO(Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public HomestayRentalVO selectVO(Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<HomestayRentalView> selectListView(Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public HomestayRentalView selectView(Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }

  @Override
  public List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectTimeStatValue(params, wrapper);
  }

  @Override
  public List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper) {
    return baseMapper.selectGroup(params, wrapper);
  }
}
