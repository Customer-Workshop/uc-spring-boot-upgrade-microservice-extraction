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
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    target = new User("target@test.com", "targetuser", "password", "bio2", "image2");
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  public void should_follow_user_success() {
    authenticateUser(user);
    ProfileData profileData =
        new ProfileData(target.getId(), "targetuser", "bio2", "image2", true);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_throw_when_following_unauthenticated() {
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  public void should_throw_when_following_nonexistent_user() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("unknown"));
  }

  @Test
  public void should_unfollow_user_success() {
    authenticateUser(user);
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    ProfileData profileData =
        new ProfileData(target.getId(), "targetuser", "bio2", "image2", false);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");
    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_throw_when_unfollowing_unauthenticated() {
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  public void should_throw_when_unfollowing_nonexistent_user() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("unknown"));
  }

  @Test
  public void should_throw_when_unfollowing_no_relation() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
