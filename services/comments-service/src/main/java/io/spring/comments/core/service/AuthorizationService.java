package io.spring.comments.core.service;

import io.spring.comments.core.comment.Comment;

public class AuthorizationService {

  private AuthorizationService() {}

  public static boolean canWriteComment(String userId, String articleAuthorId, Comment comment) {
    return userId.equals(articleAuthorId) || userId.equals(comment.getUserId());
  }
}
