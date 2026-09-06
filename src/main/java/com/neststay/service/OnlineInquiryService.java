package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.OnlineInquiryEntity;
import com.neststay.entity.view.OnlineInquiryView;
import com.neststay.entity.vo.OnlineInquiryVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 在线咨询
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface OnlineInquiryService extends IService<OnlineInquiryEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<OnlineInquiryVO> selectListVO(Wrapper<OnlineInquiryEntity> wrapper);

  OnlineInquiryVO selectVO(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  List<OnlineInquiryView> selectListView(Wrapper<OnlineInquiryEntity> wrapper);

  OnlineInquiryView selectView(@Param("ew") Wrapper<OnlineInquiryEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<OnlineInquiryEntity> wrapper);
}
