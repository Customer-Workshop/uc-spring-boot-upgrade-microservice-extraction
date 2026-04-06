package io.spring.article.infrastructure.mybatis.readservice;

import io.spring.article.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleReadService {
  ArticleData findById(@Param("id") String id);

  ArticleData findBySlug(@Param("slug") String slug);

  List<String> queryArticles(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy,
      @Param("page") io.spring.article.application.Page page);

  int countArticle(
      @Param("tag") String tag,
      @Param("author") String author,
      @Param("favoritedBy") String favoritedBy);

  List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

  List<ArticleData> findArticlesOfAuthors(
      @Param("authors") List<String> authors,
      @Param("page") io.spring.article.application.Page page);

  int countFeedSize(@Param("authors") List<String> authors);
}
