package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Maps article, tag, and article-tag relation persistence operations to SQL. */
@Mapper
public interface ArticleMapper {
  /** Inserts an article row. */
  void insert(@Param("article") Article article);

  /** Finds an article and its tags by identifier. */
  Article findById(@Param("id") String id);

  /** Finds a tag by name. */
  Tag findTag(@Param("tagName") String tagName);

  /** Inserts a tag row. */
  void insertTag(@Param("tag") Tag tag);

  /** Inserts an article-to-tag relation. */
  void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);

  /** Finds an article and its tags by slug. */
  Article findBySlug(@Param("slug") String slug);

  /** Updates an article row. */
  void update(@Param("article") Article article);

  /** Deletes an article row by identifier. */
  void delete(@Param("id") String id);
}
