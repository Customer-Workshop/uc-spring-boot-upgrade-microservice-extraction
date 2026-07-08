package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.comment.CommentResponseDto;
import io.spring.infrastructure.comment.CommentServiceClient;
import io.spring.infrastructure.comment.ProfileDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Reads comments from the extracted Comments microservice (via {@link CommentServiceClient}) and
 * adapts the responses to the monolith's {@link CommentData} read model.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private final CommentServiceClient commentServiceClient;

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient
        .findById(id, viewerId(user))
        .map(CommentQueryService::toCommentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    return commentServiceClient.findByArticleId(articleId, viewerId(user)).stream()
        .map(CommentQueryService::toCommentData)
        .collect(Collectors.toList());
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    String cursor = page.getCursor() == null ? null : page.getCursor().toString();
    List<CommentData> comments =
        commentServiceClient
            .findByArticleIdWithCursor(
                articleId, viewerId(user), cursor, page.getDirection().name())
            .stream()
            .map(CommentQueryService::toCommentData)
            .collect(Collectors.toList());
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments = new ArrayList<>(comments.subList(0, page.getLimit()));
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private static String viewerId(User user) {
    return user == null ? null : user.getId();
  }

  private static CommentData toCommentData(CommentResponseDto dto) {
    DateTime createdAt = dto.getCreatedAt() == null ? null : DateTime.parse(dto.getCreatedAt());
    DateTime updatedAt =
        dto.getUpdatedAt() == null ? createdAt : DateTime.parse(dto.getUpdatedAt());
    return new CommentData(
        dto.getId(),
        dto.getBody(),
        dto.getArticleId(),
        createdAt,
        updatedAt,
        toProfileData(dto.getAuthor()));
  }

  private static ProfileData toProfileData(ProfileDto author) {
    if (author == null) {
      return new ProfileData(null, null, null, null, false);
    }
    return new ProfileData(
        author.getId(),
        author.getUsername(),
        author.getBio(),
        author.getImage(),
        author.isFollowing());
  }
}
