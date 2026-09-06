package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ConsumerDao;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.view.ConsumerView;
import com.neststay.entity.vo.ConsumerVO;
import com.neststay.service.ConsumerService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("consumerService")
public class ConsumerServiceImpl extends ServiceImpl<ConsumerDao, ConsumerEntity>
    implements ConsumerService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<ConsumerEntity> page =
        this.page(new Query<ConsumerEntity>(params).getPage(), new QueryWrapper<ConsumerEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<ConsumerEntity> wrapper) {
    Page<ConsumerView> page = new Query<ConsumerView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<ConsumerVO> selectListVO(Wrapper<ConsumerEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public ConsumerVO selectVO(Wrapper<ConsumerEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<ConsumerView> selectListView(Wrapper<ConsumerEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public ConsumerView selectView(Wrapper<ConsumerEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
