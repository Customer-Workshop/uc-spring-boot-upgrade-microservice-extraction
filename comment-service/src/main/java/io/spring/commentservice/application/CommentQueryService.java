package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.client.UserServiceClient;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
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
    if (currentUserId != null && commentData.getProfileData() != null) {
      enrichProfileFollowing(commentData.getProfileData(), currentUserId);
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String currentUserId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (!comments.isEmpty() && currentUserId != null) {
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null) {
              enrichProfileFollowing(commentData.getProfileData(), currentUserId);
            }
          });
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String currentUserId, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (currentUserId != null) {
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null) {
              enrichProfileFollowing(commentData.getProfileData(), currentUserId);
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

  private void enrichProfileFollowing(ProfileData profile, String currentUserId) {
    try {
      boolean following = userServiceClient.isFollowing(currentUserId, profile.getId());
      profile.setFollowing(following);
    } catch (Exception e) {
      profile.setFollowing(false);
    }
  }
}
