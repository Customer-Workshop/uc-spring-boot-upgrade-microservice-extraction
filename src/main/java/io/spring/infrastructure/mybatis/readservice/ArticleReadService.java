package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Reads article projections and filtered article identifiers from joined article tables. */
@Mapper
public interface ArticleReadService {
  /** Finds an article projection by identifier, including author profile and tags. */
  ArticleData findById(@Param("id") String id);

  /** Finds an article projection by slug, including author profile and tags. */
  ArticleData findBySlug(@Param("slug") String slug);

  /** Returns newest matching article identifiers using offset and limit pagination. */
  List<String> queryArticles(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy,
      @Param("page") Page page);

  /** Counts distinct articles matching the supplied filters. */
  int countArticle(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy);

  /** Loads article projections for the supplied identifiers in creation order. */
  List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

  /** Loads offset-paginated article projections authored by the supplied users. */
  List<ArticleData> findArticlesOfAuthors(
      @Param("authors") List<String> authors, @Param("page") Page page);

  /** Loads cursor-paginated article projections authored by the supplied users. */
  List<ArticleData> findArticlesOfAuthorsWithCursor(
      @Param("authors") List<String> authors, @Param("page") CursorPageParameter page);

  /** Counts articles authored by the supplied users. */
  int countFeedSize(@Param("authors") List<String> authors);

  /** Returns filtered article identifiers around a creation-time cursor. */
  List<String> findArticlesWithCursor(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy,
      @Param("page") CursorPageParameter page);
}
