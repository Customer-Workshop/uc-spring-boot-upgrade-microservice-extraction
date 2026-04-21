package io.spring.article.core.service;

import io.spring.article.core.article.Article;

public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId.equals(article.getUserId());
  }
}
