package io.spring.core.service;

import io.spring.client.UserDto;
import io.spring.core.article.Article;
import io.spring.core.comment.Comment;

public class AuthorizationService {
  public static boolean canWriteArticle(UserDto user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  public static boolean canWriteComment(UserDto user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
