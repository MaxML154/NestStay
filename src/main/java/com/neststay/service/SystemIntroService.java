package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.SystemIntroEntity;
import com.neststay.utils.PageUtils;
import java.util.Map;

/** 系统简介 */
public interface SystemIntroService extends IService<SystemIntroEntity> {

  PageUtils queryPage(Map<String, Object> params);

  PageUtils queryPage(Map<String, Object> params, Wrapper<SystemIntroEntity> wrapper);
}
