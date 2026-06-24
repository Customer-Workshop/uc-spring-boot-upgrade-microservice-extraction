package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.client.CommentServiceClient;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Reads comments from the comments microservice. The comment data (including author profile) is
 * assembled remotely; this service only adapts the call to the monolith's domain types.
 */
@Service
public class CommentQueryService {
  private final CommentServiceClient commentServiceClient;

  public CommentQueryService(CommentServiceClient commentServiceClient) {
    this.commentServiceClient = commentServiceClient;
  }

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient.findCommentData(id, user == null ? null : user.getId());
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    return commentServiceClient.findByArticleId(articleId, user == null ? null : user.getId());
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    return commentServiceClient.findByArticleIdWithCursor(
        articleId, user == null ? null : user.getId(), page);
  }
}
