package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
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
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentResponse> responseOpt = commentServiceClient.findById(id);
    if (responseOpt.isEmpty()) {
      return Optional.empty();
    }
    CommentResponse response = responseOpt.get();
    CommentData commentData = toCommentData(response);
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
    List<CommentResponse> responses = commentServiceClient.findByArticleId(articleId);
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
    if (comments.size() > 0 && user != null) {
      List<String> authorIds =
          comments.stream()
              .filter(c -> c.getProfileData() != null)
              .map(c -> c.getProfileData().getId())
              .collect(Collectors.toList());
      if (!authorIds.isEmpty()) {
        Set<String> followingAuthors =
            userRelationshipQueryService.followingAuthors(user.getId(), authorIds);
        comments.forEach(
            commentData -> {
              if (commentData.getProfileData() != null
                  && followingAuthors.contains(commentData.getProfileData().getId())) {
                commentData.getProfileData().setFollowing(true);
              }
            });
      }
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentData> allComments = findByArticleId(articleId, user);

    List<CommentData> filtered;
    if (page.isNext()) {
      if (page.getCursor() != null) {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .collect(Collectors.toList());
      } else {
        filtered = new ArrayList<>(allComments);
      }
    } else {
      if (page.getCursor() != null) {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .collect(Collectors.toList());
        Collections.reverse(filtered);
      } else {
        filtered = new ArrayList<>(allComments);
      }
    }

    if (filtered.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
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

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profileData = new ProfileData(response.getUserId(), "unknown", "", "", false);
    UserData userData = userReadService.findById(response.getUserId());
    if (userData != null) {
      profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              false);
    }
    DateTime createdAt =
        response.getCreatedAt() != null ? DateTime.parse(response.getCreatedAt()) : new DateTime();
    DateTime updatedAt =
        response.getUpdatedAt() != null ? DateTime.parse(response.getUpdatedAt()) : createdAt;
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        updatedAt,
        profileData);
  }
}
