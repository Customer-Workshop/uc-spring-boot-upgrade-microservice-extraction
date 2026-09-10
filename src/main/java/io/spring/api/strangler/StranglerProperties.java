package io.spring.api.strangler;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Base URLs of the extracted services. When a URL is blank the monolith keeps serving the
 * corresponding routes in process.
 */
@Component
@Getter
public class StranglerProperties {
  private final String commentsServiceUrl;
  private final String favoritesServiceUrl;

  public StranglerProperties(
      @Value("${comments.service.url:${COMMENTS_SERVICE_URL:}}") String commentsServiceUrl,
      @Value("${favorites.service.url:${FAVORITES_SERVICE_URL:}}") String favoritesServiceUrl) {
    this.commentsServiceUrl = trim(commentsServiceUrl);
    this.favoritesServiceUrl = trim(favoritesServiceUrl);
  }

  public boolean isCommentsRouted() {
    return !commentsServiceUrl.isEmpty();
  }

  public boolean isFavoritesRouted() {
    return !favoritesServiceUrl.isEmpty();
  }

  private static String trim(String value) {
    if (value == null) {
      return "";
    }
    String trimmed = value.trim();
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    return trimmed;
  }
}
