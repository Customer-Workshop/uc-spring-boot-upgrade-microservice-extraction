package io.spring.comments.application;

import io.spring.comments.application.data.CommentData;
import io.spring.comments.infrastructure.client.UserServiceClient;
import io.spring.comments.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CommentQueryService {
  private final CommentReadService commentReadService;
  private final UserServiceClient userServiceClient;

  public CommentQueryService(
      CommentReadService commentReadService, UserServiceClient userServiceClient) {
    this.commentReadService = commentReadService;
    this.userServiceClient = userServiceClient;
  }

  public Optional<CommentData> findById(String id, String userId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    if (userId != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userServiceClient.isUserFollowing(userId, commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String userId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (!comments.isEmpty() && userId != null) {
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
    return comments;
  }
}
