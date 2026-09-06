package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.HomestayCategoryDao;
import com.neststay.entity.HomestayCategoryEntity;
import com.neststay.entity.view.HomestayCategoryView;
import com.neststay.entity.vo.HomestayCategoryVO;
import com.neststay.service.HomestayCategoryService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("homestayCategoryService")
public class HomestayCategoryServiceImpl
    extends ServiceImpl<HomestayCategoryDao, HomestayCategoryEntity>
    implements HomestayCategoryService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<HomestayCategoryEntity> page =
        this.page(
            new Query<HomestayCategoryEntity>(params).getPage(),
            new QueryWrapper<HomestayCategoryEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayCategoryEntity> wrapper) {
    Page<HomestayCategoryView> page = new Query<HomestayCategoryView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<HomestayCategoryVO> selectListVO(Wrapper<HomestayCategoryEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public HomestayCategoryVO selectVO(Wrapper<HomestayCategoryEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<HomestayCategoryView> selectListView(Wrapper<HomestayCategoryEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public HomestayCategoryView selectView(Wrapper<HomestayCategoryEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
