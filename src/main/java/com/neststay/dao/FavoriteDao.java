package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.view.FavoriteView;
import com.neststay.entity.vo.FavoriteVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏表
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface FavoriteDao extends BaseMapper<FavoriteEntity> {

  List<FavoriteVO> selectListVO(@Param("ew") Wrapper<FavoriteEntity> wrapper);

  FavoriteVO selectVO(@Param("ew") Wrapper<FavoriteEntity> wrapper);

  List<FavoriteView> selectListView(@Param("ew") Wrapper<FavoriteEntity> wrapper);

  List<FavoriteView> selectListView(Page page, @Param("ew") Wrapper<FavoriteEntity> wrapper);

  FavoriteView selectView(@Param("ew") Wrapper<FavoriteEntity> wrapper);
}
