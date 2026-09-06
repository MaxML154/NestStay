package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.OnlineInquiryDao;
import com.neststay.entity.OnlineInquiryEntity;
import com.neststay.entity.view.OnlineInquiryView;
import com.neststay.entity.vo.OnlineInquiryVO;
import com.neststay.service.OnlineInquiryService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("onlineInquiryService")
public class OnlineInquiryServiceImpl extends ServiceImpl<OnlineInquiryDao, OnlineInquiryEntity>
    implements OnlineInquiryService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<OnlineInquiryEntity> page =
        this.page(
            new Query<OnlineInquiryEntity>(params).getPage(),
            new QueryWrapper<OnlineInquiryEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<OnlineInquiryEntity> wrapper) {
    Page<OnlineInquiryView> page = new Query<OnlineInquiryView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<OnlineInquiryVO> selectListVO(Wrapper<OnlineInquiryEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public OnlineInquiryVO selectVO(Wrapper<OnlineInquiryEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<OnlineInquiryView> selectListView(Wrapper<OnlineInquiryEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public OnlineInquiryView selectView(Wrapper<OnlineInquiryEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
