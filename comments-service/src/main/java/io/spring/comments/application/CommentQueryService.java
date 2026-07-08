package io.spring.comments.application;

import io.spring.comments.application.data.CommentData;
import io.spring.comments.application.data.ProfileData;
import io.spring.comments.client.UserServiceClient;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.infrastructure.mybatis.mapper.CommentMapper;
import io.spring.comments.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Assembles {@link CommentData} read models by combining locally persisted comments with author
 * profiles resolved from the monolith via {@link UserServiceClient}.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private final CommentMapper commentMapper;
  private final CommentReadService commentReadService;
  private final UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String viewerId) {
    Comment comment = commentMapper.findById(id);
    if (comment == null) {
      return Optional.empty();
    }
    return Optional.of(toData(comment, viewerId));
  }

  public List<CommentData> findByArticleId(String articleId, String viewerId) {
    return commentReadService.findByArticleId(articleId).stream()
        .map(comment -> toData(comment, viewerId))
        .collect(Collectors.toList());
  }

  public List<CommentData> findByArticleIdWithCursor(
      String articleId, String viewerId, DateTime cursor, String direction) {
    return commentReadService.findByArticleIdWithCursor(articleId, cursor, direction).stream()
        .map(comment -> toData(comment, viewerId))
        .collect(Collectors.toList());
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
