package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.UserTokenEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** token */
public interface UserTokenDao extends BaseMapper<UserTokenEntity> {

  List<UserTokenEntity> selectListView(@Param("ew") Wrapper<UserTokenEntity> wrapper);

  List<UserTokenEntity> selectListView(Page page, @Param("ew") Wrapper<UserTokenEntity> wrapper);
}
