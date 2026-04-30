package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
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
    Optional<Comment> commentOpt = commentServiceClient.findByIdDirect(id);
    if (commentOpt.isEmpty()) {
      return Optional.empty();
    }
    Comment comment = commentOpt.get();
    CommentData commentData = toCommentData(comment);
    if (commentData == null) {
      return Optional.empty();
    }
    if (user != null) {
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
    List<CommentData> result =
        comments.stream()
            .map(this::toCommentData)
            .filter(cd -> cd != null)
            .collect(Collectors.toList());

    if (result.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              result.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      result.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return result;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<Comment> allComments = commentServiceClient.findByArticleId(articleId);

    List<Comment> filtered;
    if (page.getCursor() != null) {
      if (page.isNext()) {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .collect(Collectors.toList());
      } else {
        filtered =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .collect(Collectors.toList());
      }
    } else {
      filtered = new ArrayList<>(allComments);
    }

    if (page.isNext()) {
      filtered.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    } else {
      filtered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    int limit = page.getLimit();
    boolean hasExtra = filtered.size() > limit;
    if (hasExtra) {
      filtered = filtered.subList(0, limit);
    }

    List<CommentData> comments =
        filtered.stream()
            .map(this::toCommentData)
            .filter(cd -> cd != null)
            .collect(Collectors.toList());

    if (!page.isNext()) {
      Collections.reverse(comments);
    }

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

    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(Comment comment) {
    UserData userData = userReadService.findById(comment.getUserId());
    if (userData == null) {
      return null;
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getCreatedAt(),
        profileData);
  }
}
