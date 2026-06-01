package io.spring.articleservice.application.data;

import java.util.List;
import lombok.Getter;

@Getter
public class ArticleDataList {
  private final List<ArticleData> articles;
  private final int articlesCount;

  public ArticleDataList(List<ArticleData> articles, int articlesCount) {
    this.articles = articles;
    this.articlesCount = articlesCount;
  }
}
