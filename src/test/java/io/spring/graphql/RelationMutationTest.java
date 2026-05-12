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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User user;
  private User targetUser;

  @BeforeEach
  void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@test.com", "targetuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void should_follow_user_successfully() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData profileData =
        new ProfileData(targetUser.getId(), "targetuser", "bio", "image", true);
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_not_found_when_following_nonexistent_user() {
    setAuthenticated(user);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_throw_authentication_when_following_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void should_unfollow_user_successfully() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    FollowRelation followRelation = new FollowRelation(user.getId(), targetUser.getId());
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.of(followRelation));
    ProfileData profileData =
        new ProfileData(targetUser.getId(), "targetuser", "bio", "image", false);
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    verify(userRepository).removeRelation(followRelation);
  }

  @Test
  void should_throw_not_found_when_unfollowing_nonexistent_user() {
    setAuthenticated(user);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void should_throw_not_found_when_no_follow_relation_exists() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void should_throw_authentication_when_unfollowing_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
