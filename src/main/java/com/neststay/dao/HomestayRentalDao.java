package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.view.HomestayRentalView;
import com.neststay.entity.vo.HomestayRentalVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿租赁
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface HomestayRentalDao extends BaseMapper<HomestayRentalEntity> {

  List<HomestayRentalVO> selectListVO(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  HomestayRentalVO selectVO(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<HomestayRentalView> selectListView(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<HomestayRentalView> selectListView(
      Page page, @Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  HomestayRentalView selectView(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      @Param("params") Map<String, Object> params,
      @Param("ew") Wrapper<HomestayRentalEntity> wrapper);
}
