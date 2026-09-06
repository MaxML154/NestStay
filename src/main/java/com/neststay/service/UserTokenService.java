package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.UserTokenEntity;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;

/** token */
public interface UserTokenService extends IService<UserTokenEntity> {
  PageUtils queryPage(Map<String, Object> params);

  List<UserTokenEntity> selectListView(Wrapper<UserTokenEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<UserTokenEntity> wrapper);

  String generateToken(Long userid, String username, String tableName, String role);

  UserTokenEntity getTokenEntity(String token);
}
