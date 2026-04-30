package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
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
  private CommentServiceClient commentServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;
  private io.spring.infrastructure.mybatis.readservice.UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient
        .findRawById(id)
        .map(
            raw -> {
              CommentData commentData = mapToCommentData(raw);
              if (user != null && commentData.getProfileData() != null) {
                commentData
                    .getProfileData()
                    .setFollowing(
                        userRelationshipQueryService.isUserFollowing(
                            user.getId(), commentData.getProfileData().getId()));
              }
              return commentData;
            });
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<Map<String, Object>> rawComments = commentServiceClient.findByArticleId(articleId);
    List<CommentData> comments =
        rawComments.stream().map(this::mapToCommentData).collect(Collectors.toList());
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
    List<Map<String, Object>> rawComments = commentServiceClient.findByArticleId(articleId);
    List<CommentData> comments =
        rawComments.stream().map(this::mapToCommentData).collect(Collectors.toList());

    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
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
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments = new ArrayList<>(comments.subList(0, page.getLimit()));
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private CommentData mapToCommentData(Map<String, Object> raw) {
    CommentData data = new CommentData();
    data.setId((String) raw.get("id"));
    data.setBody((String) raw.get("body"));
    data.setArticleId((String) raw.get("articleId"));

    String createdAtStr = (String) raw.get("createdAt");
    if (createdAtStr != null) {
      data.setCreatedAt(DateTime.parse(createdAtStr));
      data.setUpdatedAt(DateTime.parse(createdAtStr));
    }

    String updatedAtStr = (String) raw.get("updatedAt");
    if (updatedAtStr != null) {
      data.setUpdatedAt(DateTime.parse(updatedAtStr));
    }

    String userId = (String) raw.get("userId");
    if (userId != null) {
      io.spring.application.data.UserData userData = userReadService.findById(userId);
      if (userData != null) {
        data.setProfileData(
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false));
      } else {
        data.setProfileData(new ProfileData(userId, "", "", "", false));
      }
    }
    return data;
  }
}
