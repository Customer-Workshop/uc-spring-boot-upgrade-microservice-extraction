package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
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
  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentResponse> responseOpt = commentServiceClient.findById(id);
    if (responseOpt.isEmpty()) {
      return Optional.empty();
    }
    CommentResponse response = responseOpt.get();
    CommentData commentData = toCommentData(response);
    if (commentData.getProfileData() != null) {
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
    List<CommentResponse> responses = commentServiceClient.findByArticleId(articleId);
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
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
    CommentData data = new CommentData();
    data.setId(response.getId());
    data.setBody(response.getBody());
    data.setArticleId(response.getArticleId());
    data.setCreatedAt(new DateTime(response.getCreatedAt()));
    data.setUpdatedAt(new DateTime(response.getUpdatedAt()));
    if (response.getUserId() != null) {
      var userData = userReadService.findById(response.getUserId());
      if (userData != null) {
        data.setProfileData(
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false));
      }
    }
    return data;
  }
}
