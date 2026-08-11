package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;

  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User target;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    target = new User("target@test.com", "target", "123", "target bio", "target image");
  }

  private ProfileData targetProfile(boolean following) {
    return new ProfileData(
        target.getId(), target.getUsername(), "target bio", "target image", following);
  }

  @Test
  public void should_follow_user() {
    authenticate(user);
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername(eq(target.getUsername()), eq(user)))
        .thenReturn(Optional.of(targetProfile(true)));

    ProfilePayload payload = relationMutation.follow(target.getUsername());

    assertEquals(target.getUsername(), payload.getProfile().getUsername());
    assertTrue(payload.getProfile().getFollowing());
    verify(userRepository).saveRelation(new FollowRelation(user.getId(), target.getId()));
  }

  @Test
  public void should_not_follow_unknown_user() {
    authenticate(user);
    when(userRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("ghost"));
    verify(userRepository, never()).saveRelation(any());
  }

  @Test
  public void should_not_follow_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class, () -> relationMutation.follow(target.getUsername()));
  }

  @Test
  public void should_unfollow_user() {
    authenticate(user);
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername(eq(target.getUsername()), eq(user)))
        .thenReturn(Optional.of(targetProfile(false)));

    ProfilePayload payload = relationMutation.unfollow(target.getUsername());

    assertEquals(target.getUsername(), payload.getProfile().getUsername());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_not_unfollow_unknown_user() {
    authenticate(user);
    when(userRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("ghost"));
  }

  @Test
  public void should_not_unfollow_when_relation_is_absent() {
    authenticate(user);
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.unfollow(target.getUsername()));
    verify(userRepository, never()).removeRelation(any());
  }

  @Test
  public void should_not_unfollow_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class, () -> relationMutation.unfollow(target.getUsername()));
  }
}
