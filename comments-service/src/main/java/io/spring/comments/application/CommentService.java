package io.spring.comments.application;

import io.spring.comments.application.data.CommentData;
import io.spring.comments.application.data.ProfileData;
import io.spring.comments.client.UserServiceClient;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.core.comment.CommentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
  private final CommentRepository commentRepository;
  private final UserServiceClient userServiceClient;

  public CommentService(CommentRepository commentRepository, UserServiceClient userServiceClient) {
    this.commentRepository = commentRepository;
    this.userServiceClient = userServiceClient;
  }

  public CommentData create(
      String id, String body, String userId, String articleId, DateTime createdAt) {
    Comment comment =
        (id == null)
            ? new Comment(body, userId, articleId)
            : new Comment(
                id, body, userId, articleId, createdAt == null ? new DateTime() : createdAt);
    commentRepository.save(comment);
    return toData(comment, userId);
  }

  public Optional<CommentData> findById(String id, String articleId, String viewerId) {
    Optional<Comment> comment =
        (articleId == null)
            ? commentRepository.findById(id)
            : commentRepository.findById(articleId, id);
    return comment.map(c -> toData(c, viewerId));
  }

  public List<CommentData> findByArticleId(String articleId, String viewerId) {
    return commentRepository.findByArticleId(articleId).stream()
        .map(c -> toData(c, viewerId))
        .collect(Collectors.toList());
  }

  public List<CommentData> findByArticleIdWithCursor(
      String articleId, String viewerId, DateTime cursor, int limit, boolean next) {
    return commentRepository.findByArticleIdWithCursor(articleId, cursor, limit, next).stream()
        .map(c -> toData(c, viewerId))
        .collect(Collectors.toList());
  }

  public boolean delete(String id) {
    Optional<Comment> comment = commentRepository.findById(id);
    if (comment.isPresent()) {
      commentRepository.remove(comment.get());
      return true;
    }
    return false;
  }

  private CommentData toData(Comment comment, String viewerId) {
    ProfileData author = userServiceClient.getProfile(comment.getUserId(), viewerId);
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getCreatedAt(),
        author);
  }
}
