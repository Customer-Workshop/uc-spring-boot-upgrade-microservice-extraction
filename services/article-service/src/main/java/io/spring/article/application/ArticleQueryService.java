package io.spring.article.application;

import io.spring.article.application.data.ArticleData;
import io.spring.article.application.data.ArticleDataList;
import io.spring.article.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;

  public Optional<ArticleData> findById(String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    return Optional.of(articleData);
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findArticlesOfAuthors(List<String> authors, Page page) {
    if (authors.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }
    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(authors, page);
    int count = articleReadService.countFeedSize(authors);
    return new ArticleDataList(articles, count);
  }
}
