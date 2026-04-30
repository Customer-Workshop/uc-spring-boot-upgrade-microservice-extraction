package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatsReadService {
  int getViewCount(@Param("articleId") String articleId);

  int getFavoriteCount(@Param("articleId") String articleId);

  int getCommentCount(@Param("articleId") String articleId);

  List<String> findTrendingArticleIds(@Param("limit") int limit);
}
