package io.spring.infrastructure.client;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.comment.Comment;
import io.spring.infrastructure.client.dto.RemoteComment;
import io.spring.infrastructure.client.dto.RemoteCommentData;
import io.spring.infrastructure.client.dto.RemoteCursor;
import io.spring.infrastructure.client.dto.RemoteProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** HTTP gateway to the comments microservice. */
@Component
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public CommentServiceClient(
      RestTemplate restTemplate, @Value("${comments.service.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public Comment create(Comment comment) {
    Map<String, Object> body = new HashMap<>();
    body.put("id", comment.getId());
    body.put("body", comment.getBody());
    body.put("articleId", comment.getArticleId());
    body.put("userId", comment.getUserId());
    body.put("createdAt", print(comment.getCreatedAt()));
    try {
      RemoteComment created =
          restTemplate.postForObject(baseUrl + "/comments", body, RemoteComment.class);
      return created == null ? comment : toComment(created);
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to create comment", e);
    }
  }

  public Optional<Comment> findRawById(String articleId, String id) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/{id}")
            .queryParam("articleId", articleId)
            .buildAndExpand(id)
            .toUriString();
    try {
      RemoteComment remote = restTemplate.getForObject(url, RemoteComment.class);
      return Optional.ofNullable(remote).map(this::toComment);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to load comment " + id, e);
    }
  }

  public void delete(String id) {
    try {
      restTemplate.delete(baseUrl + "/comments/{id}", id);
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to delete comment " + id, e);
    }
  }

  public Optional<CommentData> findCommentData(String id, String viewerId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/{id}/data")
            .queryParamIfPresent("viewerId", optional(viewerId))
            .buildAndExpand(id)
            .toUriString();
    try {
      RemoteCommentData remote = restTemplate.getForObject(url, RemoteCommentData.class);
      return Optional.ofNullable(remote).map(this::toCommentData);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to load comment data " + id, e);
    }
  }

  public List<CommentData> findByArticleId(String articleId, String viewerId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments")
            .queryParam("articleId", articleId)
            .queryParamIfPresent("viewerId", optional(viewerId))
            .build()
            .toUriString();
    try {
      RemoteCommentData[] remote = restTemplate.getForObject(url, RemoteCommentData[].class);
      if (remote == null) {
        return new ArrayList<>();
      }
      List<CommentData> result = new ArrayList<>();
      for (RemoteCommentData item : remote) {
        result.add(toCommentData(item));
      }
      return result;
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to load comments for article " + articleId, e);
    }
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, String viewerId, CursorPageParameter<DateTime> page) {
    String cursor = page.getCursor() == null ? null : String.valueOf(page.getCursor().getMillis());
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/cursor")
            .queryParam("articleId", articleId)
            .queryParamIfPresent("viewerId", optional(viewerId))
            .queryParamIfPresent("cursor", optional(cursor))
            .queryParam("limit", page.getLimit())
            .queryParam("direction", page.getDirection())
            .build()
            .toUriString();
    try {
      RemoteCursor remote = restTemplate.getForObject(url, RemoteCursor.class);
      List<CommentData> data =
          remote == null || remote.getComments() == null
              ? new ArrayList<>()
              : remote.getComments().stream().map(this::toCommentData).collect(Collectors.toList());
      boolean hasExtra =
          remote != null && (page.isNext() ? remote.isHasNext() : remote.isHasPrevious());
      return new CursorPager<>(data, page.getDirection(), hasExtra);
    } catch (RestClientException e) {
      throw new CommentServiceException("Failed to load comments for article " + articleId, e);
    }
  }

  private Comment toComment(RemoteComment remote) {
    return new Comment(
        remote.getId(),
        remote.getBody(),
        remote.getUserId(),
        remote.getArticleId(),
        parse(remote.getCreatedAt()));
  }

  private CommentData toCommentData(RemoteCommentData remote) {
    return new CommentData(
        remote.getId(),
        remote.getBody(),
        remote.getArticleId(),
        parse(remote.getCreatedAt()),
        parse(remote.getUpdatedAt() == null ? remote.getCreatedAt() : remote.getUpdatedAt()),
        toProfileData(remote.getAuthor()));
  }

  private ProfileData toProfileData(RemoteProfile profile) {
    if (profile == null) {
      return new ProfileData(null, null, null, null, false);
    }
    return new ProfileData(
        profile.getId(),
        profile.getUsername(),
        profile.getBio(),
        profile.getImage(),
        profile.isFollowing());
  }

  private static Optional<String> optional(String value) {
    return value == null || value.isEmpty() ? Optional.empty() : Optional.of(value);
  }

  private static String print(DateTime value) {
    return value == null ? null : ISODateTimeFormat.dateTime().withZoneUTC().print(value);
  }

  private static DateTime parse(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    return ISODateTimeFormat.dateTimeParser().withZoneUTC().parseDateTime(value);
  }
}
