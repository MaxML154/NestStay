package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.RentalCancelDao;
import com.neststay.entity.RentalCancelEntity;
import com.neststay.entity.view.RentalCancelView;
import com.neststay.entity.vo.RentalCancelVO;
import com.neststay.service.RentalCancelService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("rentalCancelService")
public class RentalCancelServiceImpl extends ServiceImpl<RentalCancelDao, RentalCancelEntity>
    implements RentalCancelService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<RentalCancelEntity> page =
        this.page(
            new Query<RentalCancelEntity>(params).getPage(),
            new QueryWrapper<RentalCancelEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<RentalCancelEntity> wrapper) {
    Page<RentalCancelView> page = new Query<RentalCancelView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<RentalCancelVO> selectListVO(Wrapper<RentalCancelEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public RentalCancelVO selectVO(Wrapper<RentalCancelEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<RentalCancelView> selectListView(Wrapper<RentalCancelEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public RentalCancelView selectView(Wrapper<RentalCancelEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
