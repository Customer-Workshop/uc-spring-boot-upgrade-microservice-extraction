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
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    // We need to search across articles, so we get all comments and filter
    // Alternatively, the microservice could support lookup by id only
    // For now, use the save response to get the comment back
    return findCommentById(id, user);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
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
    List<CommentData> allComments = findByArticleId(articleId, user);

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

    if (!page.isNext()) {
      Collections.reverse(filtered);
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

  private Optional<CommentData> findCommentById(String id, User user) {
    return commentServiceClient
        .getCommentById(id)
        .map(
            response -> {
              CommentData commentData = toCommentData(response);
              if (user != null) {
                commentData
                    .getProfileData()
                    .setFollowing(
                        userRelationshipQueryService.isUserFollowing(
                            user.getId(), commentData.getProfileData().getId()));
              }
              return commentData;
            });
  }

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profileData = buildProfileData(response.getUserId());
    DateTime createdAt;
    try {
      createdAt = DateTime.parse(response.getCreatedAt());
    } catch (Exception e) {
      createdAt = new DateTime();
    }
    DateTime updatedAt;
    try {
      updatedAt = DateTime.parse(response.getUpdatedAt());
    } catch (Exception e) {
      updatedAt = createdAt;
    }
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        updatedAt,
        profileData);
  }

  private ProfileData buildProfileData(String userId) {
    io.spring.application.data.UserData userData = userReadService.findById(userId);
    if (userData != null) {
      return new ProfileData(
          userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
    }
    return new ProfileData(userId, "", "", "", false);
  }
}
