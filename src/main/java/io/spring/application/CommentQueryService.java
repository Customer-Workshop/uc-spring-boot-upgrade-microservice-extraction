package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceClient.CommentServiceResponse;
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
    return commentServiceClient
        .getCommentById(id)
        .map(
            response -> {
              CommentData commentData = toCommentData(response);
              if (commentData.getProfileData() != null && user != null) {
                commentData
                    .getProfileData()
                    .setFollowing(
                        userRelationshipQueryService.isUserFollowing(
                            user.getId(), commentData.getProfileData().getId()));
              }
              return commentData;
            });
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentServiceResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
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
    List<CommentServiceResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);

    List<CommentData> allComments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());

    List<CommentData> filtered;
    if (page.getCursor() != null) {
      if (page.isNext()) {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .collect(Collectors.toList());
      } else {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .collect(Collectors.toList());
      }
    } else {
      filtered = new ArrayList<>(allComments);
    }

    if (page.isNext()) {
      filtered.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    } else {
      filtered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    int limit = page.getLimit();
    boolean hasExtra = filtered.size() > limit;
    if (hasExtra) {
      filtered = filtered.subList(0, limit);
    }

    if (!page.isNext()) {
      Collections.reverse(filtered);
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

    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentServiceResponse response) {
    ProfileData profileData = new ProfileData();
    if (response.getUserId() != null) {
      io.spring.application.data.UserData userData = userReadService.findById(response.getUserId());
      if (userData != null) {
        profileData.setId(userData.getId());
        profileData.setUsername(userData.getUsername());
        profileData.setBio(userData.getBio());
        profileData.setImage(userData.getImage());
      }
    }

    CommentData commentData = new CommentData();
    commentData.setId(response.getId());
    commentData.setBody(response.getBody());
    commentData.setArticleId(response.getArticleId());
    commentData.setCreatedAt(new DateTime(response.getCreatedAt().toEpochMilli()));
    commentData.setUpdatedAt(new DateTime(response.getUpdatedAt().toEpochMilli()));
    commentData.setProfileData(profileData);
    return commentData;
  }
}
