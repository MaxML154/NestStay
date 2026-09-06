package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.view.MerchantView;
import com.neststay.entity.vo.MerchantVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商家
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface MerchantDao extends BaseMapper<MerchantEntity> {

  List<MerchantVO> selectListVO(@Param("ew") Wrapper<MerchantEntity> wrapper);

  MerchantVO selectVO(@Param("ew") Wrapper<MerchantEntity> wrapper);

  List<MerchantView> selectListView(@Param("ew") Wrapper<MerchantEntity> wrapper);

  List<MerchantView> selectListView(Page page, @Param("ew") Wrapper<MerchantEntity> wrapper);

  MerchantView selectView(@Param("ew") Wrapper<MerchantEntity> wrapper);
}
