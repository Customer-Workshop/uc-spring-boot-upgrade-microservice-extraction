package io.spring.profileservice.application;

import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.application.data.UserData;
import io.spring.profileservice.client.UserServiceClient;
import io.spring.profileservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserServiceClient userServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<ProfileData> findByUsername(String username, String currentUserId) {
    Optional<UserData> userDataOpt = userServiceClient.findByUsername(username);
    if (userDataOpt.isEmpty()) {
      return Optional.empty();
    }
    UserData userData = userDataOpt.get();
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            currentUserId != null
                && userRelationshipQueryService.isUserFollowing(
                    currentUserId, userData.getId()));
    return Optional.of(profileData);
  }
}
