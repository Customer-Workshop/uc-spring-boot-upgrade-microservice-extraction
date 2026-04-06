package io.spring.article.infrastructure.mybatis.mapper;

import io.spring.article.core.Article;
import io.spring.article.core.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {
  void insert(@Param("article") Article article);

  void update(@Param("article") Article article);

  void delete(@Param("id") String id);

  Article findById(@Param("id") String id);

  Article findBySlug(@Param("slug") String slug);

  Tag findTag(@Param("tagName") String tagName);

  void insertTag(@Param("tag") Tag tag);

  void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);
}
