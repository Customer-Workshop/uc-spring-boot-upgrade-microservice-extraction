package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.infrastructure.client.CommentServiceClient;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
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
    Optional<Comment> commentOpt = commentServiceClient.findById(id);
    if (commentOpt.isEmpty()) {
      return Optional.empty();
    }
    Comment comment = commentOpt.get();
    CommentData commentData = toCommentData(comment);
    if (commentData == null) {
      return Optional.empty();
    }
    if (user != null && commentData.getProfileData() != null) {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<Comment> comments = commentServiceClient.findByArticleId(articleId);
    if (comments.isEmpty()) {
      return Collections.emptyList();
    }
    List<CommentData> commentDataList =
        comments.stream()
            .map(this::toCommentData)
            .filter(cd -> cd != null)
            .collect(Collectors.toList());

    if (!commentDataList.isEmpty() && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              commentDataList.stream()
                  .map(cd -> cd.getProfileData().getId())
                  .collect(Collectors.toList()));
      commentDataList.forEach(
          cd -> {
            if (followingAuthors.contains(cd.getProfileData().getId())) {
              cd.getProfileData().setFollowing(true);
            }
          });
    }
    return commentDataList;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<Comment> allComments = commentServiceClient.findByArticleId(articleId);

    List<Comment> filtered;
    if (page.getCursor() != null) {
      if (page.getDirection() == CursorPager.Direction.NEXT) {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
      } else {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
      }
    } else {
      if (page.getDirection() == CursorPager.Direction.NEXT) {
        filtered =
            allComments.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
      } else {
        filtered =
            allComments.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
      }
    }

    int limit = page.getLimit();
    boolean hasExtra = filtered.size() > limit;
    if (hasExtra) {
      filtered = new ArrayList<>(filtered.subList(0, limit));
    }

    List<CommentData> commentDataList =
        filtered.stream()
            .map(this::toCommentData)
            .filter(cd -> cd != null)
            .collect(Collectors.toList());

    if (!page.isNext()) {
      Collections.reverse(commentDataList);
    }

    if (!commentDataList.isEmpty() && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              commentDataList.stream()
                  .map(cd -> cd.getProfileData().getId())
                  .collect(Collectors.toList()));
      commentDataList.forEach(
          cd -> {
            if (followingAuthors.contains(cd.getProfileData().getId())) {
              cd.getProfileData().setFollowing(true);
            }
          });
    }

    return new CursorPager<>(commentDataList, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(Comment comment) {
    UserData userData = userReadService.findById(comment.getUserId());
    ProfileData profileData;
    if (userData != null) {
      profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              false);
    } else {
      profileData = new ProfileData(comment.getUserId(), "unknown", "", "", false);
    }
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getCreatedAt(),
        profileData);
  }
}
