package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.SysMenuEntity;
import com.neststay.entity.view.SysMenuView;
import com.neststay.entity.vo.SysMenuVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 菜单
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
public interface SysMenuDao extends BaseMapper<SysMenuEntity> {

  List<SysMenuVO> selectListVO(@Param("ew") Wrapper<SysMenuEntity> wrapper);

  SysMenuVO selectVO(@Param("ew") Wrapper<SysMenuEntity> wrapper);

  List<SysMenuView> selectListView(@Param("ew") Wrapper<SysMenuEntity> wrapper);

  List<SysMenuView> selectListView(Page page, @Param("ew") Wrapper<SysMenuEntity> wrapper);

  SysMenuView selectView(@Param("ew") Wrapper<SysMenuEntity> wrapper);
}
