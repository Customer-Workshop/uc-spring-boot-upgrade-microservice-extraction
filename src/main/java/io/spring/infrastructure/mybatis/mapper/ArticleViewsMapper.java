package io.spring.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleViewsMapper {
  void ensureViewRecord(@Param("articleId") String articleId);

  void incrementViewCount(@Param("articleId") String articleId);
}
