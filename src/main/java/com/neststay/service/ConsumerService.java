package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.view.ConsumerView;
import com.neststay.entity.vo.ConsumerVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 消费者
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface ConsumerService extends IService<ConsumerEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<ConsumerVO> selectListVO(Wrapper<ConsumerEntity> wrapper);

  ConsumerVO selectVO(@Param("ew") Wrapper<ConsumerEntity> wrapper);

  List<ConsumerView> selectListView(Wrapper<ConsumerEntity> wrapper);

  ConsumerView selectView(@Param("ew") Wrapper<ConsumerEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<ConsumerEntity> wrapper);
}
