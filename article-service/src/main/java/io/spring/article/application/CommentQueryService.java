package io.spring.article.application;

import io.spring.article.application.data.CommentData;
import io.spring.article.infrastructure.client.UserServiceClient;
import io.spring.article.infrastructure.mybatis.readservice.CommentReadService;
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
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String userId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      if (userId != null && commentData.getProfileData() != null) {
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
    if (comments.size() > 0 && userId != null) {
      Set<String> followingAuthors =
          userServiceClient.getFollowedUsers(userId).stream().collect(Collectors.toSet());
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null
                && followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String userId, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (userId != null) {
      Set<String> followingAuthors =
          userServiceClient.getFollowedUsers(userId).stream().collect(Collectors.toSet());
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null
                && followingAuthors.contains(commentData.getProfileData().getId())) {
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
}
