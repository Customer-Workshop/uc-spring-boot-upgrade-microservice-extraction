package io.spring.articleservice.application;

import io.spring.articleservice.application.data.CommentData;
import io.spring.articleservice.infrastructure.client.UserProfileResponse;
import io.spring.articleservice.infrastructure.client.UserServiceClient;
import io.spring.articleservice.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
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
      if (userId != null && commentData.getProfileData() != null) {
        commentData
            .getProfileData()
            .setFollowing(
                userServiceClient.isUserFollowing(userId, commentData.getProfileData().getId()));
      }
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String userId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0) {
      fillProfilesForComments(comments);
      if (userId != null) {
        Set<String> followingAuthors =
            userServiceClient.followingAuthors(
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

  private void fillProfileData(CommentData commentData) {
    if (commentData.getProfileData() != null && commentData.getProfileData().getId() != null) {
      userServiceClient
          .getUserProfile(commentData.getProfileData().getId())
          .ifPresent(
              profile -> {
                commentData.getProfileData().setUsername(profile.getUsername());
                commentData.getProfileData().setBio(profile.getBio());
                commentData.getProfileData().setImage(profile.getImage());
              });
    }
  }

  private void fillProfilesForComments(List<CommentData> comments) {
    List<String> userIds =
        comments.stream()
            .map(c -> c.getProfileData() != null ? c.getProfileData().getId() : null)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

    if (!userIds.isEmpty()) {
      Map<String, UserProfileResponse> profiles = userServiceClient.getUserProfiles(userIds);
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null) {
              UserProfileResponse profile = profiles.get(commentData.getProfileData().getId());
              if (profile != null) {
                commentData.getProfileData().setUsername(profile.getUsername());
                commentData.getProfileData().setBio(profile.getBio());
                commentData.getProfileData().setImage(profile.getImage());
              }
            }
          });
    }
  }
}
