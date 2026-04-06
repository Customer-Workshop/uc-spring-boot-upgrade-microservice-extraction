package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentResponse> response = commentServiceClient.getCommentById(id);
    if (response.isEmpty()) {
      return Optional.empty();
    }
    CommentData commentData = toCommentData(response.get());
    if (commentData != null && user != null && commentData.getProfileData() != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> comments =
        responses.stream()
            .map(this::toCommentData)
            .filter(c -> c != null)
            .collect(Collectors.toList());
    if (comments.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> comments =
        responses.stream()
            .map(this::toCommentData)
            .filter(c -> c != null)
            .collect(Collectors.toList());
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    int limit = page.getLimit();
    boolean hasExtra = comments.size() > limit;
    if (hasExtra) {
      comments = comments.subList(0, limit);
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentResponse response) {
    io.spring.application.data.UserData userData = userReadService.findById(response.getUserId());
    if (userData == null) {
      return null;
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
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
      return ISODateTimeFormat.dateTimeParser().parseDateTime(dateTimeStr);
    } catch (Exception e) {
      return new DateTime();
    }
  }
}
