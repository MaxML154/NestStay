package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.AdminUserEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 用户 */
public interface AdminUserDao extends BaseMapper<AdminUserEntity> {

  List<AdminUserEntity> selectListView(@Param("ew") Wrapper<AdminUserEntity> wrapper);

  List<AdminUserEntity> selectListView(Page page, @Param("ew") Wrapper<AdminUserEntity> wrapper);
}
