package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
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
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;
  private io.spring.infrastructure.mybatis.readservice.UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient
        .findResponseById(id)
        .map(
            response -> {
              CommentData commentData = toCommentData(response);
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

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponse> responses = commentServiceClient.findByArticleId(articleId);
    if (responses.isEmpty()) {
      return Collections.emptyList();
    }

    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());

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
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentResponse> allResponses = commentServiceClient.findByArticleId(articleId);
    List<CommentData> allComments =
        allResponses.stream().map(this::toCommentData).collect(Collectors.toList());

    List<CommentData> filtered = new ArrayList<>();
    for (CommentData cd : allComments) {
      if (page.getCursor() != null) {
        if (page.isNext() && cd.getCreatedAt().isBefore(page.getCursor())) {
          filtered.add(cd);
        } else if (!page.isNext() && cd.getCreatedAt().isAfter(page.getCursor())) {
          filtered.add(cd);
        }
      } else {
        filtered.add(cd);
      }
    }

    if (filtered.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    if (user != null) {
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
      filtered = filtered.subList(0, page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(filtered);
    }
    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentResponse response) {
    io.spring.application.data.UserData userData = userReadService.findById(response.getUserId());
    ProfileData profileData = null;
    if (userData != null) {
      profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              false);
    }
    DateTime createdAt;
    try {
      createdAt =
          response.getCreatedAt() != null
              ? ISODateTimeFormat.dateTimeParser().parseDateTime(response.getCreatedAt())
              : new DateTime();
    } catch (Exception e) {
      createdAt = new DateTime();
    }
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        createdAt,
        profileData);
  }
}
