package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

  private RelationMutation mutation;
  private User user;

  @BeforeEach
  void setUp() {
    mutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user() {
    User target = new User("target@test.com", "target", "pass", "", "");
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "target", "", "", true);
    when(profileQueryService.findByUsername(eq("target"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = mutation.follow("target");

    assertNotNull(result);
    assertEquals("target", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_when_follow_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.follow("target"));
  }

  @Test
  void should_throw_when_follow_target_not_found() {
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> mutation.follow("missing"));
  }

  @Test
  void should_unfollow_user() {
    User target = new User("target@test.com", "target", "pass", "", "");
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "target", "", "", false);
    when(profileQueryService.findByUsername(eq("target"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = mutation.unfollow("target");

    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_throw_when_unfollow_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.unfollow("target"));
  }

  @Test
  void should_throw_when_unfollow_target_not_found() {
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> mutation.unfollow("missing"));
  }

  @Test
  void should_throw_when_unfollow_relation_not_found() {
    User target = new User("target@test.com", "target", "pass", "", "");
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> mutation.unfollow("target"));
  }
}
