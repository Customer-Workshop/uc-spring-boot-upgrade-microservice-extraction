package io.spring.article.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of ProfileServiceClient. TODO: Replace with HTTP client calling Profile
 * Service.
 */
@Component
public class StubProfileServiceClient implements ProfileServiceClient {

  @Override
  public boolean isUserFollowing(String userId, String anotherUserId) {
    // TODO: Call Profile Service GET /api/internal/profiles/{userId}/following/{targetId}
    return false;
  }

  @Override
  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    // TODO: Call Profile Service GET /api/internal/profiles/{userId}/following?ids={authorIds}
    return new HashSet<>();
  }

  @Override
  public List<String> followedUsers(String userId) {
    // TODO: Call Profile Service GET /api/internal/profiles/{userId}/followed-users
    return Collections.emptyList();
  }
}
