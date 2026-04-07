package io.spring.infrastructure.service;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class HttpCommentReadService {

  private final CommentServiceClient commentServiceClient;
  private final UserReadService userReadService;

  public HttpCommentReadService(
      CommentServiceClient commentServiceClient, UserReadService userReadService) {
    this.commentServiceClient = commentServiceClient;
    this.userReadService = userReadService;
  }

  public CommentData findById(String id) {
    return commentServiceClient.findById(id).map(this::toCommentData).orElse(null);
  }

  public List<CommentData> findByArticleId(String articleId) {
    List<CommentServiceClient.CommentDto> dtos = commentServiceClient.findByArticleId(articleId);
    return dtos.stream().map(this::toCommentData).collect(Collectors.toList());
  }

  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<DateTime> page) {
    List<CommentServiceClient.CommentDto> allComments =
        commentServiceClient.findByArticleId(articleId);

    List<CommentServiceClient.CommentDto> filtered =
        allComments.stream()
            .filter(
                dto -> {
                  if (page.getCursor() == null) {
                    return true;
                  }
                  DateTime commentTime = new DateTime(dto.getCreatedAt().toEpochMilli());
                  if (page.isNext()) {
                    return commentTime.isBefore(page.getCursor());
                  } else {
                    return commentTime.isAfter(page.getCursor());
                  }
                })
            .collect(Collectors.toList());

    if (page.isNext()) {
      filtered.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    } else {
      filtered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    int limit = page.getQueryLimit();
    if (filtered.size() > limit) {
      filtered = filtered.subList(0, limit);
    }

    return filtered.stream().map(this::toCommentData).collect(Collectors.toList());
  }

  private CommentData toCommentData(CommentServiceClient.CommentDto dto) {
    ProfileData profileData = buildProfileData(dto.getUserId());
    DateTime createdAt =
        dto.getCreatedAt() != null ? new DateTime(dto.getCreatedAt().toEpochMilli()) : null;
    DateTime updatedAt =
        dto.getUpdatedAt() != null ? new DateTime(dto.getUpdatedAt().toEpochMilli()) : null;
    return new CommentData(
        dto.getId(), dto.getBody(), dto.getArticleId(), createdAt, updatedAt, profileData);
  }

  private ProfileData buildProfileData(String userId) {
    if (userId == null) {
      return new ProfileData("", "", "", "", false);
    }
    io.spring.application.data.UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return new ProfileData(userId, "", "", "", false);
    }
    return new ProfileData(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage(), false);
  }
}
