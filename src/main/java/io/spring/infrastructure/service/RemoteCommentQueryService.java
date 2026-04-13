package io.spring.infrastructure.service;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RemoteCommentQueryService extends CommentQueryService {

  private final CommentServiceClient commentServiceClient;
  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  public RemoteCommentQueryService(
      CommentServiceClient commentServiceClient,
      UserReadService userReadService,
      UserRelationshipQueryService userRelationshipQueryService) {
    super(null, userRelationshipQueryService);
    this.commentServiceClient = commentServiceClient;
    this.userReadService = userReadService;
    this.userRelationshipQueryService = userRelationshipQueryService;
  }

  @Override
  public Optional<CommentData> findById(String id, User user) {
    Optional<Map<String, Object>> rawComment = commentServiceClient.findRawById(id);
    if (rawComment.isEmpty()) {
      return Optional.empty();
    }
    CommentData commentData = toCommentData(rawComment.get());
    if (commentData.getProfileData() != null && user != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  @Override
  public List<CommentData> findByArticleId(String articleId, User user) {
    List<Map<String, Object>> rawComments = commentServiceClient.findRawByArticleId(articleId);
    List<CommentData> comments =
        rawComments.stream().map(this::toCommentData).collect(Collectors.toList());

    if (!comments.isEmpty() && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream().map(c -> c.getProfileData().getId()).collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  @Override
  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentData> allComments = findByArticleId(articleId, user);
    if (allComments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    int limit = page.getLimit();
    List<CommentData> result = allComments.stream().limit(limit + 1).collect(Collectors.toList());
    boolean hasExtra = result.size() > limit;
    if (hasExtra) {
      result = result.subList(0, limit);
    }
    if (!page.isNext()) {
      Collections.reverse(result);
    }
    return new CursorPager<>(result, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(Map<String, Object> map) {
    String userId = (String) map.get("userId");
    ProfileData profileData = buildProfileData(userId);

    String createdAtStr = (String) map.get("createdAt");
    DateTime createdAt = createdAtStr != null ? DateTime.parse(createdAtStr) : new DateTime();

    return new CommentData(
        (String) map.get("id"),
        (String) map.get("body"),
        (String) map.get("articleId"),
        createdAt,
        createdAt,
        profileData);
  }

  private ProfileData buildProfileData(String userId) {
    if (userId == null) {
      return new ProfileData("", "", "", "", false);
    }
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, "", "", "", false);
    }
    return new ProfileData(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
  }
}
