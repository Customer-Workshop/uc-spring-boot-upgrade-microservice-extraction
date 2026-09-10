package io.spring.comments.infrastructure.monolith;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.JacksonCustomizations;
import io.spring.comments.api.exception.MonolithUnavailableException;
import io.spring.comments.api.exception.ResourceNotFoundException;
import io.spring.comments.infrastructure.monolith.dto.ArticleDTO;
import io.spring.comments.infrastructure.monolith.dto.ArticleResponse;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import io.spring.comments.infrastructure.monolith.dto.ProfileResponse;
import io.spring.comments.infrastructure.monolith.dto.ProfilesResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Read-only client for the monolith's {@code /internal/**} contract. */
@Component
public class MonolithClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public MonolithClient(
      RestTemplateBuilder restTemplateBuilder,
      @Value("${monolith.base-url}") String baseUrl,
      @Value("${monolith.connect-timeout-ms:2000}") long connectTimeoutMs,
      @Value("${monolith.read-timeout-ms:5000}") long readTimeoutMs) {
    this.baseUrl = baseUrl;
    ObjectMapper objectMapper =
        new ObjectMapper()
            .registerModule(new JacksonCustomizations.RealWorldModules())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
            .setReadTimeout(Duration.ofMillis(readTimeoutMs))
            .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  public ArticleDTO findArticleBySlug(String slug) {
    String path =
        UriComponentsBuilder.fromPath("/internal/articles/{slug}")
            .buildAndExpand(slug)
            .toUriString();
    return get(path, ArticleResponse.class, ArticleResponse::getArticle);
  }

  public ProfileDTO findProfileById(String userId, String viewerId) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/internal/users/{userId}");
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    String path = builder.buildAndExpand(userId).toUriString();
    return get(path, ProfileResponse.class, ProfileResponse::getProfile);
  }

  public List<ProfileDTO> findProfilesByIds(Collection<String> userIds, String viewerId) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromPath("/internal/users")
            .queryParam("ids", String.join(",", userIds));
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    String path = builder.build().toUriString();
    return get(path, ProfilesResponse.class, ProfilesResponse::getProfiles);
  }

  private <R, T> T get(String path, Class<R> responseType, Function<R, T> extractor) {
    T value;
    try {
      R response = restTemplate.getForObject(baseUrl + path, responseType);
      value = response == null ? null : extractor.apply(response);
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResourceNotFoundException();
    } catch (RestClientException e) {
      throw new MonolithUnavailableException(path, e);
    }
    if (value == null) {
      throw new MonolithUnavailableException(path);
    }
    return value;
  }
}
