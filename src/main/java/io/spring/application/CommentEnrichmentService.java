package io.spring.application;

import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import io.spring.infrastructure.service.CommentServiceClient.CursorCommentResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentEnrichmentService {

  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User currentUser) {
    return commentServiceClient
        .getCommentById(id)
        .map(response -> enrichComment(response, currentUser));
  }

  public Optional<CommentData> findByIdAndArticleId(
      String id, String articleId, User currentUser) {
    return commentServiceClient
        .getCommentByIdAndArticleId(id, articleId)
        .map(response -> enrichComment(response, currentUser));
  }

  public List<CommentData> findByArticleId(String articleId, User currentUser) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    if (responses.isEmpty()) {
      return Collections.emptyList();
    }
    List<CommentData> comments =
        responses.stream()
            .map(response -> enrichComment(response, null))
            .collect(Collectors.toList());
    if (currentUser != null && !comments.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              currentUser.getId(),
              comments.stream()
                  .map(c -> c.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          c -> {
            if (followingAuthors.contains(c.getProfileData().getId())) {
              c.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User currentUser, DateTime cursor, Direction direction, int limit) {
    Long cursorMillis = cursor != null ? cursor.getMillis() : null;
    String dirStr = direction == Direction.NEXT ? "NEXT" : "PREV";
    CursorCommentResponse cursorResponse =
        commentServiceClient.getCommentsByArticleIdWithCursor(
            articleId, cursorMillis, dirStr, limit);
    if (cursorResponse == null || cursorResponse.getComments() == null || cursorResponse.getComments().isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), direction, false);
    }
    List<CommentData> comments =
        cursorResponse.getComments().stream()
            .map(response -> enrichComment(response, null))
            .collect(Collectors.toList());
    if (currentUser != null && !comments.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              currentUser.getId(),
              comments.stream()
                  .map(c -> c.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          c -> {
            if (followingAuthors.contains(c.getProfileData().getId())) {
              c.getProfileData().setFollowing(true);
            }
          });
    }
    if (direction != Direction.NEXT) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, direction, cursorResponse.isHasExtra());
  }

  private CommentData enrichComment(CommentResponse response, User currentUser) {
    var userData = userReadService.findById(response.getUserId());
    boolean following = false;
    if (currentUser != null && userData != null) {
      following =
          userRelationshipQueryService.isUserFollowing(currentUser.getId(), userData.getId());
    }
    ProfileData profileData =
        userData != null
            ? new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                following)
            : new ProfileData(response.getUserId(), "", "", "", false);

    DateTime createdAt = parseDateTime(response.getCreatedAt());
    DateTime updatedAt = parseDateTime(response.getUpdatedAt());

    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        updatedAt,
        profileData);
  }

  private DateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null) {
      return new DateTime();
    }
    try {
      return DateTime.parse(dateTimeStr);
    } catch (Exception e) {
      return new DateTime();
    }
  }
}
