package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.comment.CommentServiceClient;
import io.spring.infrastructure.service.comment.CommentServiceClient.CommentResponse;
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
    Optional<CommentResponse> commentOpt = commentServiceClient.getCommentById(id);
    if (commentOpt.isEmpty()) {
      return Optional.empty();
    }
    CommentResponse comment = commentOpt.get();
    CommentData commentData = toCommentData(comment);
    if (commentData == null) {
      return Optional.empty();
    }
    if (user != null && commentData.getProfileData() != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponse> comments = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> commentDataList =
        comments.stream()
            .map(this::toCommentData)
            .filter(c -> c != null)
            .collect(Collectors.toList());
    if (commentDataList.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              commentDataList.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      commentDataList.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return commentDataList;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentResponse> allComments = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> commentDataList =
        allComments.stream()
            .map(this::toCommentData)
            .filter(c -> c != null)
            .collect(Collectors.toList());

    if (commentDataList.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    List<CommentData> filtered;
    if (page.isNext()) {
      DateTime cursor = page.getCursor();
      filtered =
          commentDataList.stream()
              .filter(c -> cursor == null || c.getCreatedAt().isBefore(cursor))
              .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
              .limit(page.getLimit() + 1)
              .collect(Collectors.toList());
    } else {
      DateTime cursor = page.getCursor();
      filtered =
          commentDataList.stream()
              .filter(c -> cursor == null || c.getCreatedAt().isAfter(cursor))
              .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
              .limit(page.getLimit() + 1)
              .collect(Collectors.toList());
    }

    if (user != null && !filtered.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              filtered.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      filtered.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }

    boolean hasExtra = filtered.size() > page.getLimit();
    if (hasExtra) {
      filtered.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(filtered);
    }
    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentResponse comment) {
    UserData userData = userReadService.findById(comment.getUserId());
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
    DateTime createdAt = parseDateTime(comment.getCreatedAt());
    DateTime updatedAt = parseDateTime(comment.getUpdatedAt());
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        createdAt,
        updatedAt != null ? updatedAt : createdAt,
        profileData);
  }

  private DateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null) {
      return null;
    }
    try {
      return ISODateTimeFormat.dateTime().parseDateTime(dateTimeStr);
    } catch (Exception e) {
      try {
        return new DateTime(Long.parseLong(dateTimeStr));
      } catch (Exception e2) {
        return null;
      }
    }
  }
}
