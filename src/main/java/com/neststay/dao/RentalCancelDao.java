package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.RentalCancelEntity;
import com.neststay.entity.view.RentalCancelView;
import com.neststay.entity.vo.RentalCancelVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 租赁取消
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface RentalCancelDao extends BaseMapper<RentalCancelEntity> {

  List<RentalCancelVO> selectListVO(@Param("ew") Wrapper<RentalCancelEntity> wrapper);

  RentalCancelVO selectVO(@Param("ew") Wrapper<RentalCancelEntity> wrapper);

  List<RentalCancelView> selectListView(@Param("ew") Wrapper<RentalCancelEntity> wrapper);

  List<RentalCancelView> selectListView(
      Page page, @Param("ew") Wrapper<RentalCancelEntity> wrapper);

  RentalCancelView selectView(@Param("ew") Wrapper<RentalCancelEntity> wrapper);
}
