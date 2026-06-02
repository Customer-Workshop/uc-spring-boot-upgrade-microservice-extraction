package io.spring.articleservice.application;

import io.spring.articleservice.application.data.CommentData;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.infrastructure.client.UserServiceClient;
import io.spring.articleservice.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
      ProfileData authorProfile =
          userServiceClient.getProfile(commentData.getProfileData().getId());
      commentData.getProfileData().setUsername(authorProfile.getUsername());
      commentData.getProfileData().setBio(authorProfile.getBio());
      commentData.getProfileData().setImage(authorProfile.getImage());
      if (userId != null) {
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
    fillAuthorProfiles(comments);
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

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String userId, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    fillAuthorProfiles(comments);
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
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private void fillAuthorProfiles(List<CommentData> comments) {
    Set<String> authorIds =
        comments.stream().map(c -> c.getProfileData().getId()).collect(Collectors.toSet());
    Map<String, ProfileData> profileCache = new HashMap<>();
    authorIds.forEach(
        authorId -> profileCache.put(authorId, userServiceClient.getProfile(authorId)));
    comments.forEach(
        commentData -> {
          ProfileData cached = profileCache.get(commentData.getProfileData().getId());
          if (cached != null) {
            commentData.getProfileData().setUsername(cached.getUsername());
            commentData.getProfileData().setBio(cached.getBio());
            commentData.getProfileData().setImage(cached.getImage());
          }
        });
  }
}
