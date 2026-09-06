package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.view.HomestayInfoView;
import com.neststay.entity.vo.HomestayInfoVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿信息
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface HomestayInfoDao extends BaseMapper<HomestayInfoEntity> {

  List<HomestayInfoVO> selectListVO(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  HomestayInfoVO selectVO(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<HomestayInfoView> selectListView(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<HomestayInfoView> selectListView(
      Page page, @Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  HomestayInfoView selectView(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayInfoEntity> wrapper);
}
