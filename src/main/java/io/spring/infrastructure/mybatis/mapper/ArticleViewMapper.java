package io.spring.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleViewMapper {
  void recordView(@Param("articleId") String articleId);
}
