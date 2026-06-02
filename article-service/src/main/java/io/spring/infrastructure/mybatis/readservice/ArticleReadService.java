package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleReadService {
  ArticleData findById(@Param("id") String id);

  ArticleData findBySlug(@Param("slug") String slug);

  List<String> queryArticles(
      @Param("tag") String tag, @Param("author") String author, @Param("page") Page page);

  int countArticle(@Param("tag") String tag, @Param("author") String author);

  List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);
}
