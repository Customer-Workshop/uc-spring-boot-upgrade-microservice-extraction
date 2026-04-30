package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
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
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;
  private UserReadService userReadService;

  public Optional<CommentData> findById(String id, User user) {
    Optional<CommentResponse> response = commentServiceClient.getComment(id);
    if (response.isEmpty()) {
      return Optional.empty();
    }
    CommentResponse cr = response.get();
    CommentData commentData = toCommentData(cr);
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
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
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

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    List<CommentData> allComments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());

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

    if (user != null && !filtered.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              filtered.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      filtered.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }

    if (!page.isNext()) {
      Collections.reverse(filtered);
    }
    return new CursorPager<>(filtered, page.getDirection(), hasExtra);
  }

  private CommentData toCommentData(CommentResponse cr) {
    ProfileData profileData = new ProfileData("", "", "", "", false);
    if (cr.getUserId() != null) {
      var userData = userReadService.findById(cr.getUserId());
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

    DateTime createdAt = null;
    if (cr.getCreatedAt() != null) {
      try {
        createdAt = ISODateTimeFormat.dateTimeParser().parseDateTime(cr.getCreatedAt());
      } catch (Exception e) {
        createdAt = new DateTime();
      }
    }

    return new CommentData(
        cr.getId(), cr.getBody(), cr.getArticleId(), createdAt, createdAt, profileData);
  }
}
