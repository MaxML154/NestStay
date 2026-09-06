package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.RentalCancelEntity;
import com.neststay.entity.view.RentalCancelView;
import com.neststay.entity.vo.RentalCancelVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 租赁取消
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface RentalCancelService extends IService<RentalCancelEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<RentalCancelVO> selectListVO(Wrapper<RentalCancelEntity> wrapper);

  RentalCancelVO selectVO(@Param("ew") Wrapper<RentalCancelEntity> wrapper);

  List<RentalCancelView> selectListView(Wrapper<RentalCancelEntity> wrapper);

  RentalCancelView selectView(@Param("ew") Wrapper<RentalCancelEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<RentalCancelEntity> wrapper);
}
