package io.spring.core.service;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;

/** Static utility for checking authorization rules on domain entities. */
public class AuthorizationService {

  /**
   * Checks whether the given user is authorized to modify the article.
   *
   * @param user the user attempting the operation
   * @param article the target article
   * @return {@code true} if the user is the article's author
   */
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  /**
   * Checks whether the given user is authorized to delete a comment.
   *
   * <p>Both the article author and the comment author may delete the comment.
   *
   * @param user the user attempting the operation
   * @param article the article containing the comment
   * @param comment the target comment
   * @return {@code true} if the user is the article author or the comment author
   */
  public static boolean canWriteComment(User user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
