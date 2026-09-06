package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ChatMessageDao;
import com.neststay.entity.ChatMessageEntity;
import com.neststay.entity.view.ChatMessageView;
import com.neststay.entity.vo.ChatMessageVO;
import com.neststay.service.ChatMessageService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("chatService")
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageDao, ChatMessageEntity>
    implements ChatMessageService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<ChatMessageEntity> page =
        this.page(
            new Query<ChatMessageEntity>(params).getPage(), new QueryWrapper<ChatMessageEntity>());
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<ChatMessageEntity> wrapper) {
    Page<ChatMessageView> page = new Query<ChatMessageView>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public List<ChatMessageVO> selectListVO(Wrapper<ChatMessageEntity> wrapper) {
    return baseMapper.selectListVO(wrapper);
  }

  @Override
  public ChatMessageVO selectVO(Wrapper<ChatMessageEntity> wrapper) {
    return baseMapper.selectVO(wrapper);
  }

  @Override
  public List<ChatMessageView> selectListView(Wrapper<ChatMessageEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public ChatMessageView selectView(Wrapper<ChatMessageEntity> wrapper) {
    return baseMapper.selectView(wrapper);
  }
}
