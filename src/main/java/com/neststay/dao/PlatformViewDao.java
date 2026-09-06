package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.PlatformViewView;
import com.neststay.entity.vo.PlatformViewVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 平台民宿一览
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface PlatformViewDao extends BaseMapper<PlatformViewEntity> {

  List<PlatformViewVO> selectListVO(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  PlatformViewVO selectVO(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<PlatformViewView> selectListView(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<PlatformViewView> selectListView(
      Page page, @Param("ew") Wrapper<PlatformViewEntity> wrapper);

  PlatformViewView selectView(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<PlatformViewEntity> wrapper);
}
