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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User target;

  @BeforeEach
  void setUp() {
    user = new User("user@test.com", "user1", "password", "", "");
    target = new User("target@test.com", "target1", "password", "bio", "img");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_follow_user_success() {
    setAuthenticated(user);
    when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "target1", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("target1"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("target1");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("target1", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_when_follow_not_authenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("target1"));
  }

  @Test
  void should_throw_when_follow_user_not_found() {
    setAuthenticated(user);
    when(userRepository.findByUsername(eq("not-exists"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("not-exists"));
  }

  @Test
  void should_unfollow_user_success() {
    setAuthenticated(user);
    when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "target1", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("target1"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("target1");

    assertNotNull(result);
    assertEquals("target1", result.getProfile().getUsername());
    verify(userRepository).removeRelation(eq(relation));
  }

  @Test
  void should_throw_when_unfollow_not_authenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("target1"));
  }

  @Test
  void should_throw_when_unfollow_user_not_found() {
    setAuthenticated(user);
    when(userRepository.findByUsername(eq("not-exists"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("not-exists"));
  }

  @Test
  void should_throw_when_unfollow_no_existing_relation() {
    setAuthenticated(user);
    when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("target1"));
  }
}
