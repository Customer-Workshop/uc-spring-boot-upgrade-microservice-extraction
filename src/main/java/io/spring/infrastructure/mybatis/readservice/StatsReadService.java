package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatsReadService {
  int getCommentCount(@Param("articleId") String articleId);

  int getFavoriteCount(@Param("articleId") String articleId);

  int getViewCount(@Param("articleId") String articleId);

  void incrementViewCount(@Param("articleId") String articleId);

  List<TrendingArticleData> findTrendingArticles(
      @Param("days") int days, @Param("limit") int limit);
}
