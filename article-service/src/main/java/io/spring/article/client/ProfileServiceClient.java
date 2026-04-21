package io.spring.article.client;

import java.util.List;
import java.util.Set;

/**
 * HTTP client interface for calling the Profile/Social Service. In a full microservices deployment,
 * this would make HTTP calls to the Profile Service for follow relationships.
 *
 * <p>Expected endpoints on Profile Service:
 *
 * <ul>
 *   <li>GET /api/internal/profiles/{userId}/following?ids={authorIds} - Check which authors a user
 *       follows
 *   <li>GET /api/internal/profiles/{userId}/following/{targetId} - Check if user follows target
 *   <li>GET /api/internal/profiles/{userId}/followed-users - Get list of users followed by user
 * </ul>
 */
public interface ProfileServiceClient {

  boolean isUserFollowing(String userId, String anotherUserId);

  Set<String> followingAuthors(String userId, List<String> authorIds);

  List<String> followedUsers(String userId);
}
