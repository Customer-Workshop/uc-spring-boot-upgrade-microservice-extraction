package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import java.lang.reflect.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Comment toComment() {
    Comment comment = new Comment();
    try {
      Field idField = Comment.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(comment, this.id);

      Field bodyField = Comment.class.getDeclaredField("body");
      bodyField.setAccessible(true);
      bodyField.set(comment, this.body);

      Field userIdField = Comment.class.getDeclaredField("userId");
      userIdField.setAccessible(true);
      userIdField.set(comment, this.userId);

      Field articleIdField = Comment.class.getDeclaredField("articleId");
      articleIdField.setAccessible(true);
      articleIdField.set(comment, this.articleId);

      Field createdAtField = Comment.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(comment, this.createdAt);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to map CommentResponse to Comment", e);
    }
    return comment;
  }
}
