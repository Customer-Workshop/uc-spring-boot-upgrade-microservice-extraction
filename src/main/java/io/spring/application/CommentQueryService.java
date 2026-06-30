package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.client.CommentServiceClient;
import io.spring.infrastructure.service.client.CommentServiceResponse;
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
  private UserRelationshipQueryService userRelationshipQueryService;
  private UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentServiceResponse> resp = commentServiceClient.findCommentById(id);
    if (resp.isEmpty()) {
      return Optional.empty();
    }
    CommentServiceResponse comment = resp.get();
    CommentData commentData = toCommentData(comment);
    if (commentData.getProfileData() != null && user != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentServiceResponse> responses =
        commentServiceClient.findCommentsByArticleId(articleId);
    if (responses.isEmpty()) {
      return Collections.emptyList();
    }
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
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
    List<CommentServiceResponse> allResponses =
        commentServiceClient.findCommentsByArticleId(articleId);
    List<CommentData> allComments =
        allResponses.stream().map(this::toCommentData).collect(Collectors.toList());

    List<CommentData> filtered = new ArrayList<>();
    for (CommentData comment : allComments) {
      if (page.getCursor() != null) {
        if (page.isNext() && comment.getCreatedAt().isBefore(page.getCursor())) {
          filtered.add(comment);
        } else if (!page.isNext() && comment.getCreatedAt().isAfter(page.getCursor())) {
          filtered.add(comment);
        }
      } else {
        filtered.add(comment);
      }
    }

    if (filtered.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    if (user != null) {
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
      filtered = filtered.subList(0, page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(filtered);
    }
    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentServiceResponse response) {
    ProfileData profileData = buildProfileData(response.getUserId());
    DateTime createdAt = parseDateTime(response.getCreatedAt());
    DateTime updatedAt = parseDateTime(response.getUpdatedAt());
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        updatedAt != null ? updatedAt : createdAt,
        profileData);
  }

  private ProfileData buildProfileData(String userId) {
    if (userId == null) {
      return new ProfileData();
    }
    var userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, null, null, null, false);
    }
    return new ProfileData(
        userData.getId(),
        userData.getUsername(),
        userData.getBio(),
        userData.getImage(),
        false);
  }

  private DateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null) {
      return null;
    }
    try {
      return ISODateTimeFormat.dateTime().parseDateTime(dateTimeStr);
    } catch (Exception e) {
      return null;
    }
  }
}
