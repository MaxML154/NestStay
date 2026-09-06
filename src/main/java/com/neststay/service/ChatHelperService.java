package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.ChatHelperEntity;
import com.neststay.utils.PageUtils;
import java.util.Map;

/** 聊天助手 */
public interface ChatHelperService extends IService<ChatHelperEntity> {

  PageUtils queryPage(Map<String, Object> params);

  PageUtils queryPage(Map<String, Object> params, Wrapper<ChatHelperEntity> wrapper);
}
