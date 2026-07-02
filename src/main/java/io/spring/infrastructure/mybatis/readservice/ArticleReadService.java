package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for read-side article queries backed by XML-defined SQL. */
@Mapper
public interface ArticleReadService {

  /** Finds article read-model data by unique identifier. */
  ArticleData findById(@Param("id") String id);

  /** Finds article read-model data by slug. */
  ArticleData findBySlug(@Param("slug") String slug);

  /** Queries article IDs filtered by tag, author, and/or favorited-by with offset pagination. */
  List<String> queryArticles(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy,
      @Param("page") Page page);

  /** Counts articles matching the given filters. */
  int countArticle(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy);

  /** Retrieves article data for a list of article IDs, preserving order. */
  List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

  /** Finds articles by a list of author IDs with offset pagination. */
  List<ArticleData> findArticlesOfAuthors(
      @Param("authors") List<String> authors, @Param("page") Page page);

  /** Finds articles by a list of author IDs with cursor-based pagination. */
  List<ArticleData> findArticlesOfAuthorsWithCursor(
      @Param("authors") List<String> authors, @Param("page") CursorPageParameter page);

  /** Counts total articles written by the given authors (feed size). */
  int countFeedSize(@Param("authors") List<String> authors);

  /** Queries article IDs filtered by tag, author, and/or favorited-by with cursor pagination. */
  List<String> findArticlesWithCursor(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy,
      @Param("page") CursorPageParameter page);
}
