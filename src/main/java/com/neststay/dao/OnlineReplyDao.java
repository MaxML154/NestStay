package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.OnlineReplyEntity;
import com.neststay.entity.view.OnlineReplyView;
import com.neststay.entity.vo.OnlineReplyVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 在线回复
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface OnlineReplyDao extends BaseMapper<OnlineReplyEntity> {

  List<OnlineReplyVO> selectListVO(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  OnlineReplyVO selectVO(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  List<OnlineReplyView> selectListView(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  List<OnlineReplyView> selectListView(Page page, @Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  OnlineReplyView selectView(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);
}
