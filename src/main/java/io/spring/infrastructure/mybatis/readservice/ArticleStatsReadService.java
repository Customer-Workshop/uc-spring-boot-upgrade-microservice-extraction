package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleStatsReadService {
  ArticleStatsData findStatsBySlug(@Param("slug") String slug);

  List<TrendingArticleData> findTrendingArticles();
}
