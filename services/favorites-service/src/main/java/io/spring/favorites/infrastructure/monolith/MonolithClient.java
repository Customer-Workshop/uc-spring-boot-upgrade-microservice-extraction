package io.spring.favorites.infrastructure.monolith;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.spring.favorites.api.exception.MonolithUnavailableException;
import io.spring.favorites.api.exception.ResourceNotFoundException;
import io.spring.favorites.infrastructure.monolith.dto.ArticleDto;
import io.spring.favorites.infrastructure.monolith.dto.ArticleResponse;
import io.spring.favorites.infrastructure.monolith.dto.ProfileDto;
import io.spring.favorites.infrastructure.monolith.dto.ProfileResponse;
import java.time.Duration;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Read-only client for the monolith's {@code /internal/**} contract. Every field of the article
 * envelope except {@code favorited} / {@code favoritesCount} comes from here.
 */
@Component
public class MonolithClient {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public MonolithClient(
      RestTemplateBuilder restTemplateBuilder,
      @Value("${monolith.base-url}") String baseUrl,
      @Value("${monolith.connect-timeout-ms:2000}") long connectTimeoutMs,
      @Value("${monolith.read-timeout-ms:5000}") long readTimeoutMs) {
    this.baseUrl = baseUrl;
    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
            .setReadTimeout(Duration.ofMillis(readTimeoutMs))
            .build();
    SimpleModule jodaModule = new SimpleModule();
    jodaModule.addDeserializer(org.joda.time.DateTime.class, new DateTimeDeserializer());
    this.objectMapper =
        new ObjectMapper()
            .registerModule(jodaModule)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public ArticleDto findArticleBySlug(String slug, String viewerId) {
    String path = "/internal/articles/" + slug;
    ArticleResponse response = get(path, viewerId, ArticleResponse.class);
    if (response == null || response.getArticle() == null) {
      throw new MonolithUnavailableException(
          "GET " + path, "response did not contain an article object");
    }
    return response.getArticle();
  }

  public ProfileDto findProfileById(String userId, String viewerId) {
    String path = "/internal/users/" + userId;
    ProfileResponse response = get(path, viewerId, ProfileResponse.class);
    if (response == null || response.getProfile() == null) {
      throw new MonolithUnavailableException(
          "GET " + path, "response did not contain a profile object");
    }
    return response.getProfile();
  }

  private <T> T get(String path, String viewerId, Class<T> type) {
    UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(baseUrl).path(path);
    if (viewerId != null && !viewerId.isEmpty()) {
      uri.queryParam("viewerId", viewerId);
    }
    String endpoint = "GET " + path;
    ResponseEntity<String> response;
    try {
      response = restTemplate.getForEntity(uri.build().toUri(), String.class);
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResourceNotFoundException();
    } catch (RestClientException e) {
      throw new MonolithUnavailableException(endpoint, e);
    }
    String body = response.getBody();
    if (body == null || body.isEmpty()) {
      throw new MonolithUnavailableException(endpoint, "empty response body");
    }
    try {
      return objectMapper.readValue(body, type);
    } catch (Exception e) {
      throw new MonolithUnavailableException(endpoint, e);
    }
  }

  static class DateTimeDeserializer
      extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<org.joda.time.DateTime> {

    private static final Function<String, org.joda.time.DateTime> PARSER =
        text ->
            org.joda.time.format.ISODateTimeFormat.dateTimeParser()
                .withZoneUTC()
                .parseDateTime(text);

    DateTimeDeserializer() {
      super(org.joda.time.DateTime.class);
    }

    @Override
    public org.joda.time.DateTime deserialize(
        com.fasterxml.jackson.core.JsonParser p,
        com.fasterxml.jackson.databind.DeserializationContext ctxt)
        throws java.io.IOException {
      String text = p.getValueAsString();
      return text == null || text.isEmpty() ? null : PARSER.apply(text);
    }
  }
}
