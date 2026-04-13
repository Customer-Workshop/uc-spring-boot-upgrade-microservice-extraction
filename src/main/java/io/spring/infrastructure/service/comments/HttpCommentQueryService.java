package io.spring.infrastructure.service.comments;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.comments.CommentServiceClient.CommentResponseDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class HttpCommentQueryService extends CommentQueryService {

  private final CommentServiceClient commentServiceClient;
  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  public HttpCommentQueryService(
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
    return commentServiceClient
        .getCommentById(id)
        .map(
            dto -> {
              CommentData commentData = toCommentData(dto);
              if (commentData.getProfileData() != null && user != null) {
                commentData
                    .getProfileData()
                    .setFollowing(
                        userRelationshipQueryService.isUserFollowing(
                            user.getId(), commentData.getProfileData().getId()));
              }
              return commentData;
            });
  }

  @Override
  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponseDto> dtos = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> comments =
        dtos.stream().map(this::toCommentData).collect(Collectors.toList());

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

  @Override
  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    // For GraphQL cursor-based pagination, fetch all and paginate in memory
    List<CommentResponseDto> allDtos = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> allComments =
        allDtos.stream().map(this::toCommentData).collect(Collectors.toList());

    if (user != null && !allComments.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              allComments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      allComments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }

    // Simple cursor pagination: return up to limit items
    int limit = page.getLimit();
    List<CommentData> paged;
    if (allComments.size() > limit) {
      paged = new ArrayList<>(allComments.subList(0, limit));
    } else {
      paged = allComments;
    }

    boolean hasExtra = allComments.size() > limit;
    if (!page.isNext()) {
      Collections.reverse(paged);
    }
    return new CursorPager<>(paged, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentResponseDto dto) {
    ProfileData profileData = loadProfileData(dto.getUserId());
    DateTime createdAt = new DateTime(dto.getCreatedAt().toEpochMilli());
    DateTime updatedAt = new DateTime(dto.getUpdatedAt().toEpochMilli());
    return new CommentData(
        dto.getId(), dto.getBody(), dto.getArticleId(), createdAt, updatedAt, profileData);
  }

  private ProfileData loadProfileData(String userId) {
    io.spring.application.data.UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, "unknown", "", "", false);
    }
    return new ProfileData(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
  }
}
