package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Read-side service for querying user profile data.
 *
 * <p>Builds profile views that include whether the current user follows the queried profile.
 */
@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  /**
   * Finds a user's profile by username.
   *
   * @param username the username to look up
   * @param currentUser the current user for follow context, or {@code null} if anonymous
   * @return the profile data including follow status, or empty if the user does not exist
   */
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
