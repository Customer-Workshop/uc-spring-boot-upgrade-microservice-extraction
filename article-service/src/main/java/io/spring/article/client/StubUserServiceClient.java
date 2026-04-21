package io.spring.article.client;

import io.spring.article.application.data.ProfileData;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of UserServiceClient. TODO: Replace with HTTP client calling User Service at
 * GET /api/internal/users/{id}
 */
@Component
public class StubUserServiceClient implements UserServiceClient {

  @Override
  public Optional<ProfileData> getProfileById(String userId) {
    // TODO: Call User Service GET /api/internal/users/{id}/profile
    return Optional.empty();
  }

  @Override
  public Optional<String> getUserIdByUsername(String username) {
    // TODO: Call User Service GET /api/internal/users?username={username}
    return Optional.empty();
  }

  @Override
  public boolean userExists(String userId) {
    // TODO: Call User Service GET /api/internal/users/{id}
    return true;
  }
}
