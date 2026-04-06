package io.spring.interaction.application;

import io.spring.interaction.application.data.CommentData;
import io.spring.interaction.client.UserServiceClient;
import io.spring.interaction.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.shared.dto.ProfileData;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String currentUserId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    enrichComment(commentData, currentUserId);
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String currentUserId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    for (CommentData comment : comments) {
      enrichComment(comment, currentUserId);
    }
    return comments;
  }

  private void enrichComment(CommentData comment, String currentUserId) {
    if (comment.getUserId() != null) {
      ProfileData profile = userServiceClient.getUserProfile(comment.getUserId());
      if (currentUserId != null && profile != null) {
        profile.setFollowing(userServiceClient.isFollowing(currentUserId, comment.getUserId()));
      }
      comment.setProfileData(profile);
    }
  }
}
