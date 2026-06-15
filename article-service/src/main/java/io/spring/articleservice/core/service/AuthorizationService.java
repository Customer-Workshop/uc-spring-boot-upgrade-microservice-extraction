package io.spring.articleservice.core.service;

import io.spring.articleservice.core.article.Article;

public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId.equals(article.getUserId());
  }
}
