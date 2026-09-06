package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.NewsCategoryDao;
import com.neststay.entity.NewsCategoryEntity;
import com.neststay.entity.view.NewsCategoryView;
import com.neststay.entity.vo.NewsCategoryVO;
import com.neststay.service.NewsCategoryService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("newstypeService")
public class NewsCategoryServiceImpl extends ServiceImpl<NewsCategoryDao, NewsCategoryEntity>
    implements NewsCategoryService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<NewsCategoryEntity> page =
        this.page(
            new Query<NewsCategoryEntity>(params).getPage(),
            new QueryWrapper<NewsCategoryEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<NewsCategoryEntity> wrapper) {
    Page<NewsCategoryView> page = new Query<NewsCategoryView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<NewsCategoryVO> selectListVO(Wrapper<NewsCategoryEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public NewsCategoryVO selectVO(Wrapper<NewsCategoryEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<NewsCategoryView> selectListView(Wrapper<NewsCategoryEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public NewsCategoryView selectView(Wrapper<NewsCategoryEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
