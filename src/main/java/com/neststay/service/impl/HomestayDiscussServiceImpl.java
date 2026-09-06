package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.HomestayDiscussDao;
import com.neststay.entity.HomestayDiscussEntity;
import com.neststay.entity.view.HomestayDiscussView;
import com.neststay.entity.vo.HomestayDiscussVO;
import com.neststay.service.HomestayDiscussService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("homestayDiscussService")
public class HomestayDiscussServiceImpl
    extends ServiceImpl<HomestayDiscussDao, HomestayDiscussEntity>
    implements HomestayDiscussService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<HomestayDiscussEntity> page =
        this.page(
            new Query<HomestayDiscussEntity>(params).getPage(),
            new QueryWrapper<HomestayDiscussEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayDiscussEntity> wrapper) {
    Page<HomestayDiscussView> page = new Query<HomestayDiscussView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<HomestayDiscussVO> selectListVO(Wrapper<HomestayDiscussEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public HomestayDiscussVO selectVO(Wrapper<HomestayDiscussEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<HomestayDiscussView> selectListView(Wrapper<HomestayDiscussEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public HomestayDiscussView selectView(Wrapper<HomestayDiscussEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
