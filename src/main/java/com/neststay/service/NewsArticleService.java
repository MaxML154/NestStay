package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.view.NewsArticleView;
import com.neststay.entity.vo.NewsArticleVO;
import com.neststay.utils.PageUtils;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿资讯
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface NewsArticleService extends IService<NewsArticleEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<NewsArticleVO> selectListVO(Wrapper<NewsArticleEntity> wrapper);

  NewsArticleVO selectVO(@Param("ew") Wrapper<NewsArticleEntity> wrapper);

  List<NewsArticleView> selectListView(Wrapper<NewsArticleEntity> wrapper);

  NewsArticleView selectView(@Param("ew") Wrapper<NewsArticleEntity> wrapper);

  PageUtils queryPage(Map<String, Object> params, Wrapper<NewsArticleEntity> wrapper);
}
