package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.ReviewInfoEntity;
import com.neststay.entity.view.ReviewInfoView;
import com.neststay.entity.vo.ReviewInfoVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 评价信息
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ReviewInfoService extends IService<ReviewInfoEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<ReviewInfoVO> selectListVO(Wrapper<ReviewInfoEntity> wrapper);

  ReviewInfoVO selectVO(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  List<ReviewInfoView> selectListView(Wrapper<ReviewInfoEntity> wrapper);

  ReviewInfoView selectView(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<ReviewInfoEntity> wrapper);
}
