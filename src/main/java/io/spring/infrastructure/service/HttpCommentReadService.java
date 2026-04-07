package io.spring.infrastructure.service;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class HttpCommentReadService implements CommentReadService {

  private final CommentServiceClient commentServiceClient;
  private final UserReadService userReadService;

  public HttpCommentReadService(
      CommentServiceClient commentServiceClient, UserReadService userReadService) {
    this.commentServiceClient = commentServiceClient;
    this.userReadService = userReadService;
  }

  @Override
  public CommentData findById(String id) {
    return commentServiceClient
        .getCommentById(id, null)
        .map(this::toCommentData)
        .orElse(null);
  }

  @Override
  public List<CommentData> findByArticleId(String articleId) {
    List<CommentResponse> responses = commentServiceClient.getCommentsByArticleId(articleId);
    return responses.stream().map(this::toCommentData).collect(Collectors.toList());
  }

  @Override
  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<DateTime> page) {
    List<CommentData> all = findByArticleId(articleId);
    if (all.isEmpty()) {
      return Collections.emptyList();
    }
    // Apply cursor-based filtering
    DateTime cursor = page.getCursor();
    if (cursor == null) {
      return applyLimit(all, page);
    }
    List<CommentData> filtered = new ArrayList<>();
    for (CommentData comment : all) {
      if (page.isNext()) {
        if (comment.getCreatedAt().isBefore(cursor)) {
          filtered.add(comment);
        }
      } else {
        if (comment.getCreatedAt().isAfter(cursor)) {
          filtered.add(comment);
        }
      }
    }
    return applyLimit(filtered, page);
  }

  private List<CommentData> applyLimit(List<CommentData> list, CursorPageParameter<DateTime> page) {
    int limit = page.getQueryLimit();
    if (list.size() <= limit) {
      return list;
    }
    return list.subList(0, limit);
  }

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profileData = buildProfileData(response.getUserId());
    DateTime createdAt = parseInstant(response.getCreatedAt());
    DateTime updatedAt = parseInstant(response.getUpdatedAt());
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        createdAt,
        updatedAt,
        profileData);
  }

  private ProfileData buildProfileData(String userId) {
    if (userId == null) {
      return new ProfileData(null, "unknown", "", "", false);
    }
    var userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, "unknown", "", "", false);
    }
    return new ProfileData(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
  }

  private DateTime parseInstant(String instantStr) {
    if (instantStr == null) {
      return new DateTime();
    }
    try {
      return new DateTime(Instant.parse(instantStr).toEpochMilli());
    } catch (Exception e) {
      return new DateTime();
    }
  }
}
