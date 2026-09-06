package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.HomestayCategoryEntity;
import com.neststay.entity.view.HomestayCategoryView;
import com.neststay.entity.vo.HomestayCategoryVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿类型
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface HomestayCategoryService extends IService<HomestayCategoryEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<HomestayCategoryVO> selectListVO(Wrapper<HomestayCategoryEntity> wrapper);

  HomestayCategoryVO selectVO(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  List<HomestayCategoryView> selectListView(Wrapper<HomestayCategoryEntity> wrapper);

  HomestayCategoryView selectView(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayCategoryEntity> wrapper);
}
