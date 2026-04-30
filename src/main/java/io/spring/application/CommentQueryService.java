package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
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
    List<Comment> comments = commentServiceClient.getCommentsByArticleId(null);
    Comment comment = null;
    for (Comment c : comments) {
      if (c.getId().equals(id)) {
        comment = c;
        break;
      }
    }
    if (comment == null) {
      return Optional.empty();
    }
    CommentData commentData = buildCommentData(comment);
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

  public Optional<CommentData> findById(String commentId, String articleId, User user) {
    Optional<Comment> commentOpt =
        commentServiceClient.getCommentByIdAndArticleId(articleId, commentId);
    if (commentOpt.isEmpty()) {
      return Optional.empty();
    }
    Comment comment = commentOpt.get();
    CommentData commentData = buildCommentData(comment);
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
    List<Comment> comments = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> commentDataList = new ArrayList<>();
    for (Comment comment : comments) {
      CommentData data = buildCommentData(comment);
      if (data != null) {
        commentDataList.add(data);
      }
    }
    if (commentDataList.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              commentDataList.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      commentDataList.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return commentDataList;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<Comment> allComments = commentServiceClient.getCommentsByArticleId(articleId);

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

    int limit = page.getLimit() + 1;
    List<Comment> paged = filtered.subList(0, Math.min(limit, filtered.size()));

    List<CommentData> commentDataList = new ArrayList<>();
    for (Comment comment : paged) {
      CommentData data = buildCommentData(comment);
      if (data != null) {
        commentDataList.add(data);
      }
    }

    if (commentDataList.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              commentDataList.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      commentDataList.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }

    boolean hasExtra = commentDataList.size() > page.getLimit();
    if (hasExtra) {
      commentDataList.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(commentDataList);
    }
    return new CursorPager<>(commentDataList, page.getDirection(), hasExtra);
  }

  private CommentData buildCommentData(Comment comment) {
    io.spring.application.data.UserData userData = userReadService.findById(comment.getUserId());
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
    CommentData commentData = new CommentData();
    commentData.setId(comment.getId());
    commentData.setBody(comment.getBody());
    commentData.setArticleId(comment.getArticleId());
    commentData.setCreatedAt(comment.getCreatedAt());
    commentData.setUpdatedAt(comment.getCreatedAt());
    commentData.setProfileData(profileData);
    return commentData;
  }
}
