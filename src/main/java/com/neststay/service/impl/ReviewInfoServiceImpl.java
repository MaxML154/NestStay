package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ReviewInfoDao;
import com.neststay.entity.ReviewInfoEntity;
import com.neststay.entity.view.ReviewInfoView;
import com.neststay.entity.vo.ReviewInfoVO;
import com.neststay.service.ReviewInfoService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("reviewInfoService")
public class ReviewInfoServiceImpl extends ServiceImpl<ReviewInfoDao, ReviewInfoEntity>
    implements ReviewInfoService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<ReviewInfoEntity> page =
        this.page(
            new Query<ReviewInfoEntity>(params).getPage(), new QueryWrapper<ReviewInfoEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<ReviewInfoEntity> wrapper) {
    Page<ReviewInfoView> page = new Query<ReviewInfoView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<ReviewInfoVO> selectListVO(Wrapper<ReviewInfoEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public ReviewInfoVO selectVO(Wrapper<ReviewInfoEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<ReviewInfoView> selectListView(Wrapper<ReviewInfoEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public ReviewInfoView selectView(Wrapper<ReviewInfoEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
