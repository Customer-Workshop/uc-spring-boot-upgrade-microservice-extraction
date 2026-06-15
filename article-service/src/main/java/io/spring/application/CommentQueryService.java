package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.client.UserServiceClient;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String userId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      fillProfileData(commentData);
      if (userId != null) {
        commentData
            .getProfileData()
            .setFollowing(
                userServiceClient.isFollowing(userId, commentData.getProfileData().getId()));
      }
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String userId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0) {
      fillProfilesData(comments);
      if (userId != null) {
        Set<String> followingAuthors =
            userServiceClient.getFollowingAuthors(
                userId,
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
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String userId, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    fillProfilesData(comments);
    if (userId != null) {
      Set<String> followingAuthors =
          userServiceClient.getFollowingAuthors(
              userId,
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
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private void fillProfileData(CommentData commentData) {
    if (commentData.getProfileData() != null && commentData.getProfileData().getId() != null) {
      ProfileData profile = userServiceClient.getProfile(commentData.getProfileData().getId());
      commentData.setProfileData(profile);
    }
  }

  private void fillProfilesData(List<CommentData> comments) {
    List<String> userIds =
        comments.stream()
            .filter(c -> c.getProfileData() != null && c.getProfileData().getId() != null)
            .map(c -> c.getProfileData().getId())
            .distinct()
            .collect(Collectors.toList());
    if (userIds.isEmpty()) {
      return;
    }
    Map<String, ProfileData> profiles = userServiceClient.getProfiles(userIds);
    comments.forEach(
        commentData -> {
          if (commentData.getProfileData() != null
              && commentData.getProfileData().getId() != null) {
            ProfileData profile = profiles.get(commentData.getProfileData().getId());
            if (profile != null) {
              commentData.setProfileData(profile);
            }
          }
        });
  }
}
