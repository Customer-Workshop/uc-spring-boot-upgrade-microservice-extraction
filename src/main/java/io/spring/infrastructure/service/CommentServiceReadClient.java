package io.spring.infrastructure.service;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
public class CommentServiceReadClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;
  private final UserReadService userReadService;

  public CommentServiceReadClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url}") String commentsServiceUrl,
      UserReadService userReadService) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
    this.userReadService = userReadService;
  }

  public CommentData findById(String id) {
    try {
      CommentResponse response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}", CommentResponse.class, id);
      if (response == null) {
        return null;
      }
      return toCommentData(response);
    } catch (Exception e) {
      return null;
    }
  }

  public List<CommentData> findByArticleId(String articleId) {
    try {
      CommentResponse[] responses =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments?articleId={articleId}",
              CommentResponse[].class,
              articleId);
      if (responses == null) {
        return Collections.emptyList();
      }
      return Arrays.stream(responses).map(this::toCommentData).collect(Collectors.toList());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profileData = null;
    if (response.getUserId() != null) {
      io.spring.application.data.UserData userData = userReadService.findById(response.getUserId());
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
    if (profileData == null) {
      profileData = new ProfileData(response.getUserId(), "", "", "", false);
    }
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        response.getCreatedAt(),
        response.getUpdatedAt(),
        profileData);
  }
}
