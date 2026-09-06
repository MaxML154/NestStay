package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.NewsCategoryEntity;
import com.neststay.entity.view.NewsCategoryView;
import com.neststay.entity.vo.NewsCategoryVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿资讯分类
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface NewsCategoryService extends IService<NewsCategoryEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<NewsCategoryVO> selectListVO(Wrapper<NewsCategoryEntity> wrapper);

  NewsCategoryVO selectVO(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  List<NewsCategoryView> selectListView(Wrapper<NewsCategoryEntity> wrapper);

  NewsCategoryView selectView(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<NewsCategoryEntity> wrapper);
}
