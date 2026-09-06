package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ForumPostDao;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.view.ForumPostView;
import com.neststay.entity.vo.ForumPostVO;
import com.neststay.service.ForumPostService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("forumService")
public class ForumPostServiceImpl extends ServiceImpl<ForumPostDao, ForumPostEntity>
    implements ForumPostService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<ForumPostEntity> page =
        this.page(
            new Query<ForumPostEntity>(params).getPage(), new QueryWrapper<ForumPostEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<ForumPostEntity> wrapper) {
    Page<ForumPostView> page = new Query<ForumPostView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<ForumPostVO> selectListVO(Wrapper<ForumPostEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public ForumPostVO selectVO(Wrapper<ForumPostEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<ForumPostView> selectListView(Wrapper<ForumPostEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public ForumPostView selectView(Wrapper<ForumPostEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
