package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.view.ConsumerView;
import com.neststay.entity.vo.ConsumerVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 消费者
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
public interface ConsumerDao extends BaseMapper<ConsumerEntity> {

  List<ConsumerVO> selectListVO(@Param("ew") Wrapper<ConsumerEntity> wrapper);

  ConsumerVO selectVO(@Param("ew") Wrapper<ConsumerEntity> wrapper);

  List<ConsumerView> selectListView(@Param("ew") Wrapper<ConsumerEntity> wrapper);

  List<ConsumerView> selectListView(Page page, @Param("ew") Wrapper<ConsumerEntity> wrapper);

  ConsumerView selectView(@Param("ew") Wrapper<ConsumerEntity> wrapper);
}
