package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentResponse;
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
  private UserRelationshipQueryService userRelationshipQueryService;
  private UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentResponse> responseOpt = commentServiceClient.findResponseById(id);
    if (responseOpt.isEmpty()) {
      return Optional.empty();
    }
    CommentResponse response = responseOpt.get();
    CommentData commentData = toCommentData(response);
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
    List<CommentResponse> responses = commentServiceClient.findByArticleId(articleId);
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
    if (comments.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .filter(c -> c.getProfileData() != null)
                  .map(commentData -> commentData.getProfileData().getId())
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
    List<CommentResponse> responses = commentServiceClient.findByArticleId(articleId);
    List<CommentData> allComments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());

    List<CommentData> comments;
    if (page.getCursor() != null) {
      if (page.isNext()) {
        comments =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isBefore(page.getCursor()))
                .limit(page.getQueryLimit())
                .collect(Collectors.toList());
      } else {
        comments =
            allComments.stream()
                .filter(c -> c.getCreatedAt().isAfter(page.getCursor()))
                .limit(page.getQueryLimit())
                .collect(Collectors.toList());
      }
    } else {
      comments = allComments.stream().limit(page.getQueryLimit()).collect(Collectors.toList());
    }

    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .filter(c -> c.getProfileData() != null)
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (commentData.getProfileData() != null
                && followingAuthors.contains(commentData.getProfileData().getId())) {
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

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profileData = null;
    if (response.getUserId() != null) {
      UserData userData = userReadService.findById(response.getUserId());
      if (userData != null) {
        profileData =
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false);
      }
    }
    DateTime createdAt =
        response.getCreatedAt() != null ? DateTime.parse(response.getCreatedAt()) : new DateTime();
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        createdAt,
        profileData);
  }
}
