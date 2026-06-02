package io.spring.article.infrastructure.client;

import io.spring.article.application.data.ProfileData;
import io.spring.article.infrastructure.mybatis.mapper.UserProfileMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserProfileCacheService {
  private final UserServiceClient userServiceClient;
  private final UserProfileMapper userProfileMapper;

  public UserProfileCacheService(
      UserServiceClient userServiceClient, UserProfileMapper userProfileMapper) {
    this.userServiceClient = userServiceClient;
    this.userProfileMapper = userProfileMapper;
  }

  public void ensureProfileCached(String userId) {
    if (userId == null) {
      return;
    }
    Optional<ProfileData> profile = userServiceClient.getProfileByUserId(userId);
    profile.ifPresent(
        p ->
            userProfileMapper.upsert(
                p.getId() != null ? p.getId() : userId,
                p.getUsername(),
                p.getBio(),
                p.getImage()));
  }
}
