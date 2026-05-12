package io.spring.commentservice.core.service;

import io.spring.commentservice.core.article.Article;
import io.spring.commentservice.core.comment.Comment;
import io.spring.commentservice.core.user.User;

public class AuthorizationService {
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  public static boolean canWriteComment(User user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
