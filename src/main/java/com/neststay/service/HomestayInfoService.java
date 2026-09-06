package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.view.HomestayInfoView;
import com.neststay.entity.vo.HomestayInfoVO;
import com.neststay.utils.PageUtils;
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
public interface HomestayInfoService extends IService<HomestayInfoEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<HomestayInfoVO> selectListVO(Wrapper<HomestayInfoEntity> wrapper);

  HomestayInfoVO selectVO(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  List<HomestayInfoView> selectListView(Wrapper<HomestayInfoEntity> wrapper);

  HomestayInfoView selectView(@Param("ew") Wrapper<HomestayInfoEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<HomestayInfoEntity> wrapper);
}
