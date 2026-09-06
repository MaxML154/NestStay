package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.OnlineInquiryEntity;
import com.neststay.entity.view.OnlineInquiryView;
import com.neststay.entity.vo.OnlineInquiryVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 在线咨询
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface OnlineInquiryDao extends BaseMapper<OnlineInquiryEntity> {

  List<OnlineInquiryVO> selectListVO(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  OnlineInquiryVO selectVO(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  List<OnlineInquiryView> selectListView(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  List<OnlineInquiryView> selectListView(
      Page page, @Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  OnlineInquiryView selectView(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);
}
