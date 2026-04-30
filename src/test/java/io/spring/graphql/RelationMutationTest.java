package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User currentUser;
  private User targetUser;

  @BeforeEach
  void setUp() {
    currentUser = new User("me@example.com", "me", "pass", "bio", "image");
    targetUser = new User("target@example.com", "target", "pass", "", "");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(currentUser, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user_success() {
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));
    ProfileData profileData = new ProfileData(targetUser.getId(), "target", "", "", true);
    when(profileQueryService.findByUsername("target", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("target");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("target", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_follow_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(NullPointerException.class, () -> relationMutation.follow("target"));
  }

  @Test
  void should_follow_throw_when_user_not_found() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_unfollow_user_success() {
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));
    FollowRelation relation = new FollowRelation(currentUser.getId(), targetUser.getId());
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(targetUser.getId(), "target", "", "", false);
    when(profileQueryService.findByUsername("target", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("target");

    assertNotNull(result);
    assertFalse(result.getProfile().getFollowing());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_unfollow_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(NullPointerException.class, () -> relationMutation.unfollow("target"));
  }

  @Test
  void should_unfollow_throw_when_user_not_found() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void should_unfollow_throw_when_no_relation() {
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("target"));
  }
}
