package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.HomestayCategoryEntity;
import com.neststay.entity.view.HomestayCategoryView;
import com.neststay.entity.vo.HomestayCategoryVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿类型
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface HomestayCategoryDao extends BaseMapper<HomestayCategoryEntity> {

  List<HomestayCategoryVO> selectListVO(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  HomestayCategoryVO selectVO(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  List<HomestayCategoryView> selectListView(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  List<HomestayCategoryView> selectListView(
      Page page, @Param("ew") Wrapper<HomestayCategoryEntity> wrapper);

  HomestayCategoryView selectView(@Param("ew") Wrapper<HomestayCategoryEntity> wrapper);
}
