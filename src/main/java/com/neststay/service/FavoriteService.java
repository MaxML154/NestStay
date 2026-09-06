package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.view.FavoriteView;
import com.neststay.entity.vo.FavoriteVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏表
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface FavoriteService extends IService<FavoriteEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<FavoriteVO> selectListVO(Wrapper<FavoriteEntity> wrapper);

  FavoriteVO selectVO(@Param("ew") Wrapper<FavoriteEntity> wrapper);

  List<FavoriteView> selectListView(Wrapper<FavoriteEntity> wrapper);

  FavoriteView selectView(@Param("ew") Wrapper<FavoriteEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<FavoriteEntity> wrapper);
}
