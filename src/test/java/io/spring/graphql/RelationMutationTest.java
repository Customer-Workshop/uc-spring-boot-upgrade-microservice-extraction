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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class RelationMutationTest {

  private RelationMutation relationMutation;
  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;
  private User user;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    profileQueryService = mock(ProfileQueryService.class);
    relationMutation = new RelationMutation(userRepository, profileQueryService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user_successfully() {
    User target = new User("target@example.com", "targetuser", "password", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));

    ProfileData profileData =
        new ProfileData(target.getId(), "targetuser", "", "", true);
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_when_following_nonexistent_user() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_throw_when_following_without_auth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void should_unfollow_user_successfully() {
    User target = new User("target@example.com", "targetuser", "password", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));

    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.of(relation));

    ProfileData profileData =
        new ProfileData(target.getId(), "targetuser", "", "", false);
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertFalse(result.getProfile().getFollowing());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_throw_when_unfollowing_nonexistent_user() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void should_throw_when_unfollow_relation_not_found() {
    User target = new User("target@example.com", "targetuser", "password", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
