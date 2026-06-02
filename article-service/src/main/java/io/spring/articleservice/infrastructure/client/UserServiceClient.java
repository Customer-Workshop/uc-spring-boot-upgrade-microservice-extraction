package io.spring.articleservice.infrastructure.client;

import java.util.List;
import java.util.Set;

public interface UserServiceClient {
  UserProfileDto findUserProfileById(String userId);

  boolean isUserFollowing(String userId, String targetUserId);

  Set<String> followingAuthors(String userId, List<String> authorIds);

  List<String> followedUsers(String userId);
}
