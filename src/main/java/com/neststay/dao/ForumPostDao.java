package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.view.ForumPostView;
import com.neststay.entity.vo.ForumPostVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 社区论坛
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ForumPostDao extends BaseMapper<ForumPostEntity> {

  List<ForumPostVO> selectListVO(@Param("ew") Wrapper<ForumPostEntity> wrapper);

  ForumPostVO selectVO(@Param("ew") Wrapper<ForumPostEntity> wrapper);

  List<ForumPostView> selectListView(@Param("ew") Wrapper<ForumPostEntity> wrapper);

  List<ForumPostView> selectListView(Page page, @Param("ew") Wrapper<ForumPostEntity> wrapper);

  ForumPostView selectView(@Param("ew") Wrapper<ForumPostEntity> wrapper);
}
