package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.HomestayDiscussEntity;
import com.neststay.entity.view.HomestayDiscussView;
import com.neststay.entity.vo.HomestayDiscussVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * minsuxinxi评论表
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
public interface HomestayDiscussDao extends BaseMapper<HomestayDiscussEntity> {

  List<HomestayDiscussVO> selectListVO(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  HomestayDiscussVO selectVO(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  List<HomestayDiscussView> selectListView(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  List<HomestayDiscussView> selectListView(
      Page page, @Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  HomestayDiscussView selectView(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);
}
