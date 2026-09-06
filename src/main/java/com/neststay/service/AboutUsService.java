package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.AboutUsEntity;
import com.neststay.utils.PageUtils;
import java.util.Map;

/** 关于我们 */
public interface AboutUsService extends IService<AboutUsEntity> {

  PageUtils queryPage(Map<String, Object> params);

  PageUtils queryPage(Map<String, Object> params, Wrapper<AboutUsEntity> wrapper);
}
