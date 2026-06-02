package io.spring.article.core.service;

import io.spring.article.core.article.Article;
import io.spring.article.core.comment.Comment;

public class AuthorizationService {
  public static boolean canWriteArticle(String userId, Article article) {
    return userId.equals(article.getUserId());
  }

  public static boolean canWriteComment(String userId, Article article, Comment comment) {
    return userId.equals(article.getUserId()) || userId.equals(comment.getUserId());
  }
}
