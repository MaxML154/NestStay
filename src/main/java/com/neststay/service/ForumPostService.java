package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.view.ForumPostView;
import com.neststay.entity.vo.ForumPostVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 社区论坛
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ForumPostService extends IService<ForumPostEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<ForumPostVO> selectListVO(Wrapper<ForumPostEntity> wrapper);

  ForumPostVO selectVO(@Param("ew") Wrapper<ForumPostEntity> wrapper);

  List<ForumPostView> selectListView(Wrapper<ForumPostEntity> wrapper);

  ForumPostView selectView(@Param("ew") Wrapper<ForumPostEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<ForumPostEntity> wrapper);
}
