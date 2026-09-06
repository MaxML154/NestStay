package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.FavoriteDao;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.view.FavoriteView;
import com.neststay.entity.vo.FavoriteVO;
import com.neststay.service.FavoriteService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("storeupService")
public class FavoriteServiceImpl extends ServiceImpl<FavoriteDao, FavoriteEntity>
    implements FavoriteService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<FavoriteEntity> page =
        this.page(new Query<FavoriteEntity>(params).getPage(), new QueryWrapper<FavoriteEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<FavoriteEntity> wrapper) {
    Page<FavoriteView> page = new Query<FavoriteView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<FavoriteVO> selectListVO(Wrapper<FavoriteEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public FavoriteVO selectVO(Wrapper<FavoriteEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<FavoriteView> selectListView(Wrapper<FavoriteEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public FavoriteView selectView(Wrapper<FavoriteEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
