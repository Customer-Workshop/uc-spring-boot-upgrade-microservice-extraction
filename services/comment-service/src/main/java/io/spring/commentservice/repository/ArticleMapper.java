package io.spring.commentservice.repository;

import io.spring.commentservice.domain.Article;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {
  Article findBySlug(@Param("slug") String slug);
}
