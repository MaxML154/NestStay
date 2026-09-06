package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ChatMessageEntity;
import com.neststay.entity.view.ChatMessageView;
import com.neststay.entity.vo.ChatMessageVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 联系平台客服
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ChatMessageDao extends BaseMapper<ChatMessageEntity> {

  List<ChatMessageVO> selectListVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

  ChatMessageVO selectVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

  List<ChatMessageView> selectListView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

  List<ChatMessageView> selectListView(Page page, @Param("ew") Wrapper<ChatMessageEntity> wrapper);

  ChatMessageView selectView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
}
