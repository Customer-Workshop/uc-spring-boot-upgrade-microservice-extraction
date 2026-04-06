package io.spring.user.application;

import io.spring.shared.dto.ProfileData;
import io.spring.shared.dto.UserData;
import io.spring.user.core.User;
import io.spring.user.infrastructure.mybatis.readservice.UserReadService;
import io.spring.user.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<ProfileData> findByUsername(String username, User currentUser) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    } else {
      ProfileData profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              currentUser != null
                  && userRelationshipQueryService.isUserFollowing(
                      currentUser.getId(), userData.getId()));
      return Optional.of(profileData);
    }
  }
}
