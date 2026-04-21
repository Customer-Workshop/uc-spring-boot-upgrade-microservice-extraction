package io.spring.article.client;

import io.spring.article.application.data.ProfileData;
import java.util.Optional;

/**
 * HTTP client interface for calling the User Service. In a full microservices deployment, this
 * would make HTTP calls to the User Service to resolve user/profile data.
 *
 * <p>Expected endpoints on User Service:
 *
 * <ul>
 *   <li>GET /api/internal/users/{id} - Get user by ID
 *   <li>GET /api/internal/users/{id}/profile - Get user profile by ID
 * </ul>
 */
public interface UserServiceClient {

  Optional<ProfileData> getProfileById(String userId);

  Optional<String> getUserIdByUsername(String username);

  boolean userExists(String userId);
}
