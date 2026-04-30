package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentDto;
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
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentDto> commentDto = commentServiceClient.getCommentById(id);
    if (commentDto.isEmpty()) {
      return Optional.empty();
    }
    CommentData commentData = toCommentData(commentDto.get());
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
    List<CommentDto> commentDtos = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> comments =
        commentDtos.stream().map(this::toCommentData).collect(Collectors.toList());
    if (comments.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .filter(id -> id != null)
                  .collect(Collectors.toList()));
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
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentDto> allDtos = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> allComments =
        allDtos.stream().map(this::toCommentData).collect(Collectors.toList());

    List<CommentData> filtered = applyCursorPaging(allComments, page);

    if (!filtered.isEmpty() && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              filtered.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .filter(id -> id != null)
                  .collect(Collectors.toList()));
      filtered.forEach(
          commentData -> {
            if (commentData.getProfileData() != null
                && followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }

    boolean hasExtra = filtered.size() > page.getLimit();
    if (hasExtra) {
      filtered = new ArrayList<>(filtered.subList(0, page.getLimit()));
    }
    if (!page.isNext()) {
      Collections.reverse(filtered);
    }
    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private List<CommentData> applyCursorPaging(
      List<CommentData> comments, CursorPageParameter<DateTime> page) {
    java.util.stream.Stream<CommentData> stream =
        comments.stream()
            .filter(
                c -> {
                  if (page.getCursor() == null) {
                    return true;
                  }
                  if (page.isNext()) {
                    return c.getCreatedAt().isBefore(page.getCursor());
                  } else {
                    return c.getCreatedAt().isAfter(page.getCursor());
                  }
                });
    if (!page.isNext()) {
      stream = stream.sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }
    return stream.limit(page.getLimit() + 1).collect(Collectors.toList());
  }

  private CommentData toCommentData(CommentDto dto) {
    ProfileData profileData = buildProfileData(dto.getUserId());
    DateTime createdAt = dto.getCreatedAtDateTime();
    return new CommentData(
        dto.getId(), dto.getBody(), dto.getArticleId(), createdAt, createdAt, profileData);
  }

  private ProfileData buildProfileData(String userId) {
    if (userId == null) {
      return new ProfileData(null, null, null, null, false);
    }
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, null, null, null, false);
    }
    return new ProfileData(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
  }
}
