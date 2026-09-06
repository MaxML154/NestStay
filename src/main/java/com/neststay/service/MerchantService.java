package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.view.MerchantView;
import com.neststay.entity.vo.MerchantVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 商家
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface MerchantService extends IService<MerchantEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<MerchantVO> selectListVO(Wrapper<MerchantEntity> wrapper);

  MerchantVO selectVO(@Param("ew") Wrapper<MerchantEntity> wrapper);

  List<MerchantView> selectListView(Wrapper<MerchantEntity> wrapper);

  MerchantView selectView(@Param("ew") Wrapper<MerchantEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<MerchantEntity> wrapper);
}
