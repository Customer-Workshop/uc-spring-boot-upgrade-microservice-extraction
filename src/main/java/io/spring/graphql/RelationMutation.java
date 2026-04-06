package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@DgsComponent
public class RelationMutation {

  private final ProfileQueryService profileQueryService;
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public RelationMutation(
      ProfileQueryService profileQueryService,
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.profileQueryService = profileQueryService;
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
  public ProfilePayload follow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);

    // Delegate follow operation to user-service
    restTemplate.postForObject(
        userServiceUrl + "/profiles/{username}/follow",
        HttpEntity.EMPTY,
        String.class,
        username);

    Profile profile = buildProfile(username, user);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
  public ProfilePayload unfollow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);

    // Delegate unfollow operation to user-service
    restTemplate.exchange(
        userServiceUrl + "/profiles/{username}/follow",
        HttpMethod.DELETE,
        HttpEntity.EMPTY,
        String.class,
        username);

    Profile profile = buildProfile(username, user);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  private Profile buildProfile(String username, User current) {
    ProfileData profileData =
        profileQueryService
            .findByUsername(username, current)
            .orElseThrow(ResourceNotFoundException::new);
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
