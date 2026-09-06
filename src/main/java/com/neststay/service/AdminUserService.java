package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.AdminUserEntity;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;

/** 系统用户 */
public interface AdminUserService extends IService<AdminUserEntity> {
  PageUtils queryPage(Map<String, Object> params);

  List<AdminUserEntity> selectListView(Wrapper<AdminUserEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<AdminUserEntity> wrapper);
}
