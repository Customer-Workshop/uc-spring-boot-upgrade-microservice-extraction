package io.spring.articleservice.core.service;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.user.User;

public class AuthorizationService {
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }
}
