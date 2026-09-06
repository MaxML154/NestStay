package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.ChatMessageEntity;
import com.neststay.entity.view.ChatMessageView;
import com.neststay.entity.vo.ChatMessageVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 联系平台客服
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ChatMessageService extends IService<ChatMessageEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<ChatMessageVO> selectListVO(Wrapper<ChatMessageEntity> wrapper);

  ChatMessageVO selectVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

  List<ChatMessageView> selectListView(Wrapper<ChatMessageEntity> wrapper);

  ChatMessageView selectView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<ChatMessageEntity> wrapper);
}
