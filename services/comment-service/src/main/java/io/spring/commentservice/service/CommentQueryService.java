package io.spring.commentservice.service;

import io.spring.commentservice.domain.CommentData;
import io.spring.commentservice.domain.User;
import io.spring.commentservice.repository.CommentReadService;
import io.spring.commentservice.repository.UserRelationshipQueryService;
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
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
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
}
