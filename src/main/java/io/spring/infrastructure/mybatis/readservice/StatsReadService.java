package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatsReadService {
  Map<String, Object> getArticleStatsBySlug(@Param("slug") String slug);

  List<TrendingArticleData> getTrendingArticles(@Param("days") int days, @Param("limit") int limit);
}
