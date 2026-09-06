package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.view.PlatformViewView;
import com.neststay.entity.vo.PlatformViewVO;
import com.neststay.utils.PageUtils;
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
public interface PlatformViewService extends IService<PlatformViewEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<PlatformViewVO> selectListVO(Wrapper<PlatformViewEntity> wrapper);

  PlatformViewVO selectVO(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  List<PlatformViewView> selectListView(Wrapper<PlatformViewEntity> wrapper);

  PlatformViewView selectView(@Param("ew") Wrapper<PlatformViewEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectValue(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectTimeStatValue(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper);

  List<Map<String, Object>> selectGroup(
      Map<String, Object> params, Wrapper<PlatformViewEntity> wrapper);
}
