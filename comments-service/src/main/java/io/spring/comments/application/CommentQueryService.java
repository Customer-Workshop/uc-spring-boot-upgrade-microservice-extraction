package io.spring.comments.application;

import io.spring.comments.application.data.CommentData;
import io.spring.comments.application.data.ProfileData;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.infrastructure.client.UserServiceClient;
import io.spring.comments.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
public class CommentQueryService {
  private final CommentReadService commentReadService;
  private final UserServiceClient userServiceClient;

  public CommentQueryService(
      CommentReadService commentReadService, UserServiceClient userServiceClient) {
    this.commentReadService = commentReadService;
    this.userServiceClient = userServiceClient;
  }

  public Optional<CommentData> findById(String id, String viewerId) {
    Comment comment = commentReadService.findById(id);
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

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String viewerId, CursorPageParameter<DateTime> page) {
    List<Comment> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    List<CommentData> data =
        comments.stream().map(comment -> toData(comment, viewerId)).collect(Collectors.toList());
    return new CursorPager<>(data, page.getDirection(), hasExtra);
  }

  private CommentData toData(Comment comment, String viewerId) {
    ProfileData author = userServiceClient.getProfile(comment.getUserId(), viewerId);
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getCreatedAt(),
        author);
  }
}
