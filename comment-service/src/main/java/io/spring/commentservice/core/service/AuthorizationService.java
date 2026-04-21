package io.spring.commentservice.core.service;

import io.spring.commentservice.core.comment.Comment;

public class AuthorizationService {

  public static boolean canWriteComment(
      String currentUserId, String articleUserId, Comment comment) {
    return currentUserId.equals(articleUserId) || currentUserId.equals(comment.getUserId());
  }
}
