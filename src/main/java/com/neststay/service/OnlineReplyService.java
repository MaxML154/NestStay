package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.OnlineReplyEntity;
import com.neststay.entity.view.OnlineReplyView;
import com.neststay.entity.vo.OnlineReplyVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 在线回复
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface OnlineReplyService extends IService<OnlineReplyEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<OnlineReplyVO> selectListVO(Wrapper<OnlineReplyEntity> wrapper);

  OnlineReplyVO selectVO(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  List<OnlineReplyView> selectListView(Wrapper<OnlineReplyEntity> wrapper);

  OnlineReplyView selectView(@Param("ew") Wrapper<OnlineReplyEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<OnlineReplyEntity> wrapper);
}
