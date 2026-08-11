package io.spring.graphql;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User currentUser;
  private User target;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    currentUser = new User("a@test.com", "a", "123", "", "");
    target = new User("b@test.com", "b", "123", "bio", "image");
    profileData = new ProfileData(target.getId(), target.getUsername(), "bio", "image", true);
  }

  @Test
  public void should_follow_user_success() {
    setCurrentUser(currentUser);
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername(eq(target.getUsername()), eq(currentUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.follow(target.getUsername());

    Assertions.assertEquals(target.getUsername(), payload.getProfile().getUsername());
    verify(userRepository).saveRelation(new FollowRelation(currentUser.getId(), target.getId()));
  }

  @Test
  public void should_not_follow_without_login() {
    setAnonymousUser();

    Assertions.assertThrows(
        AuthenticationException.class, () -> relationMutation.follow(target.getUsername()));
    verify(userRepository, never()).saveRelation(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void should_not_follow_unknown_user() {
    setCurrentUser(currentUser);
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.follow("unknown"));
  }

  @Test
  public void should_unfollow_user_success() {
    setCurrentUser(currentUser);
    FollowRelation relation = new FollowRelation(currentUser.getId(), target.getId());
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(currentUser.getId()), eq(target.getId())))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername(eq(target.getUsername()), eq(currentUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.unfollow(target.getUsername());

    Assertions.assertEquals(target.getUsername(), payload.getProfile().getUsername());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_not_unfollow_without_login() {
    setAnonymousUser();

    Assertions.assertThrows(
        AuthenticationException.class, () -> relationMutation.unfollow(target.getUsername()));
  }

  @Test
  public void should_not_unfollow_unknown_user() {
    setCurrentUser(currentUser);
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.unfollow("unknown"));
  }

  @Test
  public void should_not_unfollow_when_relation_is_missing() {
    setCurrentUser(currentUser);
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(currentUser.getId()), eq(target.getId())))
        .thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.unfollow(target.getUsername()));
    verify(userRepository, never()).removeRelation(org.mockito.ArgumentMatchers.any());
  }
}
