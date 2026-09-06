package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.OnlineReplyDao;
import com.neststay.entity.OnlineReplyEntity;
import com.neststay.entity.view.OnlineReplyView;
import com.neststay.entity.vo.OnlineReplyVO;
import com.neststay.service.OnlineReplyService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("onlineReplyService")
public class OnlineReplyServiceImpl extends ServiceImpl<OnlineReplyDao, OnlineReplyEntity>
    implements OnlineReplyService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<OnlineReplyEntity> page =
        this.page(
            new Query<OnlineReplyEntity>(params).getPage(), new QueryWrapper<OnlineReplyEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<OnlineReplyEntity> wrapper) {
    Page<OnlineReplyView> page = new Query<OnlineReplyView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<OnlineReplyVO> selectListVO(Wrapper<OnlineReplyEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public OnlineReplyVO selectVO(Wrapper<OnlineReplyEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<OnlineReplyView> selectListView(Wrapper<OnlineReplyEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public OnlineReplyView selectView(Wrapper<OnlineReplyEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
