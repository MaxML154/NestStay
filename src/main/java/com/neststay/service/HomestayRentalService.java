package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.view.HomestayRentalView;
import com.neststay.entity.vo.HomestayRentalVO;
import com.neststay.utils.PageUtils;
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
public interface HomestayRentalService extends IService<HomestayRentalEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<HomestayRentalVO> selectListVO(Wrapper<HomestayRentalEntity> wrapper);

  HomestayRentalVO selectVO(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  List<HomestayRentalView> selectListView(Wrapper<HomestayRentalEntity> wrapper);

  HomestayRentalView selectView(@Param("ew") Wrapper<HomestayRentalEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<HomestayRentalEntity> wrapper);
}
