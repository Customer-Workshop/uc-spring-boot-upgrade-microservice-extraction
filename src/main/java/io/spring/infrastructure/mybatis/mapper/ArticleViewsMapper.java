package io.spring.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleViewsMapper {
  void upsertViewCount(@Param("articleId") String articleId);
}
