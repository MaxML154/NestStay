package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.SysMenuEntity;
import com.neststay.entity.view.SysMenuView;
import com.neststay.entity.vo.SysMenuVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 菜单
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
public interface SysMenuService extends IService<SysMenuEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<SysMenuVO> selectListVO(Wrapper<SysMenuEntity> wrapper);

  SysMenuVO selectVO(@Param("ew") Wrapper<SysMenuEntity> wrapper);

  List<SysMenuView> selectListView(Wrapper<SysMenuEntity> wrapper);

  SysMenuView selectView(@Param("ew") Wrapper<SysMenuEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<SysMenuEntity> wrapper);
}
