package io.spring.article.api.dto;

import io.spring.article.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private DateTime createdAt;

  public static CommentResponse from(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .userId(comment.getUserId())
        .articleId(comment.getArticleId())
        .createdAt(comment.getCreatedAt())
        .build();
  }
}
