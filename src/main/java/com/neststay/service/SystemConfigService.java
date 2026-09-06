package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.SystemConfigEntity;
import com.neststay.utils.PageUtils;
import java.util.Map;

/** 系统用户 */
public interface SystemConfigService extends IService<SystemConfigEntity> {
  PageUtils queryPage(Map<String, Object> params, Wrapper<SystemConfigEntity> wrapper);
}
