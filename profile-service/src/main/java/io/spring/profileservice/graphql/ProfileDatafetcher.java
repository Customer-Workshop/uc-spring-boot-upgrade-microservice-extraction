package io.spring.profileservice.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.profileservice.api.exception.ResourceNotFoundException;
import io.spring.profileservice.application.ProfileQueryService;
import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.graphql.DgsConstants.QUERY;
import io.spring.profileservice.graphql.types.Profile;
import io.spring.profileservice.graphql.types.ProfilePayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ProfileDatafetcher {

  private ProfileQueryService profileQueryService;

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Profile)
  public ProfilePayload queryProfile(@InputArgument("username") String username) {
    Profile profile = buildProfile(username);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  private Profile buildProfile(String username) {
    String currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
    ProfileData profileData =
        profileQueryService
            .findByUsername(username, currentUserId)
            .orElseThrow(ResourceNotFoundException::new);
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
