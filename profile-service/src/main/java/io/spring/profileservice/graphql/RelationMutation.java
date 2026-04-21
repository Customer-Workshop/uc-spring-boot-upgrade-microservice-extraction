package io.spring.profileservice.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.profileservice.api.exception.ResourceNotFoundException;
import io.spring.profileservice.application.ProfileQueryService;
import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.application.data.UserData;
import io.spring.profileservice.client.UserServiceClient;
import io.spring.profileservice.core.FollowRelation;
import io.spring.profileservice.core.FollowRelationRepository;
import io.spring.profileservice.graphql.DgsConstants.MUTATION;
import io.spring.profileservice.graphql.exception.AuthenticationException;
import io.spring.profileservice.graphql.types.Profile;
import io.spring.profileservice.graphql.types.ProfilePayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class RelationMutation {

  private UserServiceClient userServiceClient;
  private FollowRelationRepository followRelationRepository;
  private ProfileQueryService profileQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
  public ProfilePayload follow(@InputArgument("username") String username) {
    String currentUserId = SecurityUtil.getCurrentUserId().orElseThrow(AuthenticationException::new);
    UserData target =
        userServiceClient.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    FollowRelation followRelation = new FollowRelation(currentUserId, target.getId());
    followRelationRepository.saveRelation(followRelation);
    Profile profile = buildProfile(username, currentUserId);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
  public ProfilePayload unfollow(@InputArgument("username") String username) {
    String currentUserId = SecurityUtil.getCurrentUserId().orElseThrow(AuthenticationException::new);
    UserData target =
        userServiceClient.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    return followRelationRepository
        .findRelation(currentUserId, target.getId())
        .map(
            relation -> {
              followRelationRepository.removeRelation(relation);
              Profile profile = buildProfile(username, currentUserId);
              return ProfilePayload.newBuilder().profile(profile).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Profile buildProfile(String username, String currentUserId) {
    ProfileData profileData = profileQueryService.findByUsername(username, currentUserId).get();
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
