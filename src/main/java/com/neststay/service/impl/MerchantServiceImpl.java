package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.MerchantDao;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.view.MerchantView;
import com.neststay.entity.vo.MerchantVO;
import com.neststay.service.MerchantService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("merchantService")
public class MerchantServiceImpl extends ServiceImpl<MerchantDao, MerchantEntity>
    implements MerchantService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<MerchantEntity> page =
        this.page(new Query<MerchantEntity>(params).getPage(), new QueryWrapper<MerchantEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<MerchantEntity> wrapper) {
    Page<MerchantView> page = new Query<MerchantView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<MerchantVO> selectListVO(Wrapper<MerchantEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public MerchantVO selectVO(Wrapper<MerchantEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<MerchantView> selectListView(Wrapper<MerchantEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public MerchantView selectView(Wrapper<MerchantEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
