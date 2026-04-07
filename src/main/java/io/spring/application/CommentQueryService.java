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
    Optional<Comment> commentOpt = commentServiceClient.findByCommentId(id);
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
    List<CommentData> commentDataList =
        comments.stream()
            .map(this::toCommentData)
            .filter(c -> c != null)
            .collect(Collectors.toList());

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
    List<CommentData> allComments = findByArticleId(articleId, user);

    List<CommentData> filtered = new ArrayList<>(allComments);
    if (page.getCursor() != null) {
      if (page.isNext()) {
        filtered =
            filtered.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .collect(Collectors.toList());
      } else {
        filtered =
            filtered.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .collect(Collectors.toList());
      }
    }

    if (page.isNext()) {
      filtered.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    } else {
      filtered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    boolean hasExtra = filtered.size() > page.getLimit();
    if (filtered.size() > page.getLimit()) {
      filtered = filtered.subList(0, page.getLimit());
    }

    if (!page.isNext()) {
      Collections.reverse(filtered);
    }

    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
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
    DateTime createdAt =
        comment.getCreatedAt() != null ? new DateTime(comment.getCreatedAt().getMillis()) : null;
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        createdAt,
        createdAt,
        profileData);
  }
}
