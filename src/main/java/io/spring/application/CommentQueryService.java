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
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<Map<String, Object>> optionalComment = commentServiceClient.findCommentById(id);
    if (optionalComment.isEmpty()) {
      return Optional.empty();
    }
    Map<String, Object> raw = optionalComment.get();
    CommentData commentData = mapToCommentData(raw);
    if (commentData.getProfileData() != null && user != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
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
    if (hasExtra && comments.size() > page.getLimit()) {
      comments = new ArrayList<>(comments.subList(0, page.getLimit()));
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private CommentData mapToCommentData(Map<String, Object> raw) {
    String id = (String) raw.get("id");
    String body = (String) raw.get("body");
    String articleId = (String) raw.get("articleId");
    String userId = (String) raw.get("userId");

    DateTime createdAt = parseDateTime(raw.get("createdAt"));
    DateTime updatedAt = parseDateTime(raw.get("updatedAt"));

    ProfileData profileData = new ProfileData(userId, "", "", "", false);

    return new CommentData(id, body, articleId, createdAt, updatedAt, profileData);
  }

  private DateTime parseDateTime(Object value) {
    if (value == null) {
      return new DateTime();
    }
    try {
      return ISODateTimeFormat.dateTimeParser().parseDateTime(value.toString());
    } catch (Exception e) {
      return new DateTime();
    }
  }
}
