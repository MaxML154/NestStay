package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.NewsCategoryEntity;
import com.neststay.entity.view.NewsCategoryView;
import com.neststay.entity.vo.NewsCategoryVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿资讯分类
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface NewsCategoryDao extends BaseMapper<NewsCategoryEntity> {

  List<NewsCategoryVO> selectListVO(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  NewsCategoryVO selectVO(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  List<NewsCategoryView> selectListView(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  List<NewsCategoryView> selectListView(
      Page page, @Param("ew") Wrapper<NewsCategoryEntity> wrapper);

  NewsCategoryView selectView(@Param("ew") Wrapper<NewsCategoryEntity> wrapper);
}
