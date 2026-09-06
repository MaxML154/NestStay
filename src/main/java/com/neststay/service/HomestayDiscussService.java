package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.HomestayDiscussEntity;
import com.neststay.entity.view.HomestayDiscussView;
import com.neststay.entity.vo.HomestayDiscussVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * minsuxinxi评论表
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
public interface HomestayDiscussService extends IService<HomestayDiscussEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<HomestayDiscussVO> selectListVO(Wrapper<HomestayDiscussEntity> wrapper);

  HomestayDiscussVO selectVO(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  List<HomestayDiscussView> selectListView(Wrapper<HomestayDiscussEntity> wrapper);

  HomestayDiscussView selectView(@Param("ew") Wrapper<HomestayDiscussEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<HomestayDiscussEntity> wrapper);
}
