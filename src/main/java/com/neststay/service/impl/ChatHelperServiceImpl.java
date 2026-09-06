package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.ChatHelperDao;
import com.neststay.entity.ChatHelperEntity;
import com.neststay.service.ChatHelperService;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("chathelperService")
public class ChatHelperServiceImpl extends ServiceImpl<ChatHelperDao, ChatHelperEntity>
    implements ChatHelperService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<ChatHelperEntity> pageResult =
        this.page(
            new Query<ChatHelperEntity>(params).getPage(), new QueryWrapper<ChatHelperEntity>());
    return new PageUtils(pageResult);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<ChatHelperEntity> wrapper) {
    Page<ChatHelperEntity> pageResult =
        this.page(new Query<ChatHelperEntity>(params).getPage(), wrapper);
    PageUtils pageUtil = new PageUtils(pageResult);
    return pageUtil;
  }
}
