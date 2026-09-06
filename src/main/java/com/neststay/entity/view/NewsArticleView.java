package com.neststay.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.neststay.entity.NewsArticleEntity;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 * 民宿资讯 后端返回视图实体辅助类 （通常后端关联的表或者自定义的字段需要返回使用）
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@TableName("news")
public class NewsArticleView extends NewsArticleEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public NewsArticleView() {}

  public NewsArticleView(NewsArticleEntity newsEntity) {
    try {
      BeanUtils.copyProperties(this, newsEntity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
