package com.neststay.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.view.NewsArticleView;
import com.neststay.entity.vo.NewsArticleVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 民宿资讯
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
public interface NewsArticleDao extends BaseMapper<NewsArticleEntity> {

  List<NewsArticleVO> selectListVO(@Param("ew") Wrapper<NewsArticleEntity> wrapper);

  NewsArticleVO selectVO(@Param("ew") Wrapper<NewsArticleEntity> wrapper);

  List<NewsArticleView> selectListView(@Param("ew") Wrapper<NewsArticleEntity> wrapper);

  List<NewsArticleView> selectListView(Page page, @Param("ew") Wrapper<NewsArticleEntity> wrapper);

  NewsArticleView selectView(@Param("ew") Wrapper<NewsArticleEntity> wrapper);
}
