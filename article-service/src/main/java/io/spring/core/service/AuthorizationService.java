package io.spring.core.service;

import io.spring.core.article.Article;

public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId.equals(article.getUserId());
  }
}
