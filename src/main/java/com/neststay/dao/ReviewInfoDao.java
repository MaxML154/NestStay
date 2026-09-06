package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ReviewInfoEntity;
import com.neststay.entity.view.ReviewInfoView;
import com.neststay.entity.vo.ReviewInfoVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 评价信息
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface ReviewInfoDao extends BaseMapper<ReviewInfoEntity> {

  List<ReviewInfoVO> selectListVO(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  ReviewInfoVO selectVO(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  List<ReviewInfoView> selectListView(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  List<ReviewInfoView> selectListView(Page page, @Param("ew") Wrapper<ReviewInfoEntity> wrapper);

  ReviewInfoView selectView(@Param("ew") Wrapper<ReviewInfoEntity> wrapper);
}
