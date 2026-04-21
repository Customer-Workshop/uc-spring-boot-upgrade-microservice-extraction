package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.client.UserServiceClient;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
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
    }
    if (userId != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userServiceClient.isUserFollowing(
                  userId, commentData.getProfileData().getId(), null));
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String userId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0 && userId != null) {
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
