package io.spring.infrastructure.rest;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.comment.Comment;
import io.spring.infrastructure.rest.dto.CommentEnvelope;
import io.spring.infrastructure.rest.dto.CommentResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client used by the monolith to talk to the extracted comments microservice. All comment
 * persistence and retrieval is delegated to that service.
 */
@Component
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public CommentServiceClient(
      RestTemplate commentServiceRestTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String baseUrl) {
    this.restTemplate = commentServiceRestTemplate;
    this.baseUrl = baseUrl;
  }

  public void create(Comment comment) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("id", comment.getId());
    payload.put("body", comment.getBody());
    payload.put("userId", comment.getUserId());
    payload.put("articleId", comment.getArticleId());
    payload.put("createdAt", printDateTime(comment.getCreatedAt()));
    restTemplate.postForObject(baseUrl + "/comments", payload, CommentEnvelope.Single.class);
  }

  public Optional<Comment> findRawById(String articleId, String id) {
    CommentEnvelope.Single envelope =
        restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/comments/{id}")
                .queryParam("articleId", articleId)
                .buildAndExpand(id)
                .toUri(),
            CommentEnvelope.Single.class);
    if (envelope == null || envelope.getComment() == null) {
      return Optional.empty();
    }
    return Optional.of(toComment(envelope.getComment()));
  }

  public Optional<CommentData> findCommentDataById(String id, String viewerId) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path("/comments/{id}");
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    CommentEnvelope.Single envelope =
        restTemplate.getForObject(builder.buildAndExpand(id).toUri(), CommentEnvelope.Single.class);
    if (envelope == null || envelope.getComment() == null) {
      return Optional.empty();
    }
    return Optional.of(toCommentData(envelope.getComment()));
  }

  public List<CommentData> findByArticleId(String articleId, String viewerId) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments")
            .queryParam("articleId", articleId);
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    return toCommentDataList(
        restTemplate.getForObject(builder.build().toUri(), CommentEnvelope.Multiple.class));
  }

  public List<CommentData> findByArticleIdWithCursor(
      String articleId, String viewerId, DateTime cursor, int limit, boolean next) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments")
            .queryParam("articleId", articleId)
            .queryParam("limit", limit)
            .queryParam("direction", next ? "NEXT" : "PREV");
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    if (cursor != null) {
      builder.queryParam("cursor", printDateTime(cursor));
    }
    return toCommentDataList(
        restTemplate.getForObject(builder.build().toUri(), CommentEnvelope.Multiple.class));
  }

  public void delete(String id) {
    URI uri =
        UriComponentsBuilder.fromHttpUrl(baseUrl).path("/comments/{id}").buildAndExpand(id).toUri();
    restTemplate.delete(uri);
  }

  private List<CommentData> toCommentDataList(CommentEnvelope.Multiple envelope) {
    if (envelope == null || envelope.getComments() == null) {
      return new ArrayList<>();
    }
    return envelope.getComments().stream().map(this::toCommentData).collect(Collectors.toList());
  }

  private Comment toComment(CommentResponse response) {
    return new Comment(
        response.getId(),
        response.getBody(),
        response.getUserId(),
        response.getArticleId(),
        parseDateTime(response.getCreatedAt()));
  }

  private CommentData toCommentData(CommentResponse response) {
    ProfileData profile = new ProfileData();
    if (response.getAuthor() != null) {
      profile =
          new ProfileData(
              response.getAuthor().getId(),
              response.getAuthor().getUsername(),
              response.getAuthor().getBio(),
              response.getAuthor().getImage(),
              response.getAuthor().isFollowing());
    }
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        parseDateTime(response.getCreatedAt()),
        parseDateTime(response.getUpdatedAt()),
        profile);
  }

  private String printDateTime(DateTime dateTime) {
    return dateTime == null ? null : ISODateTimeFormat.dateTime().withZoneUTC().print(dateTime);
  }

  private DateTime parseDateTime(String value) {
    return value == null
        ? null
        : ISODateTimeFormat.dateTimeParser().parseDateTime(value).withZone(DateTimeZone.UTC);
  }
}
