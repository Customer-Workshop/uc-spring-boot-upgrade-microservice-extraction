package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User targetUser;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@test.com", "targetuser", "password", "bio2", "image2");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Test
  void should_follow_user_success() {
    ProfileData profileData =
        new ProfileData(targetUser.getId(), "targetuser", "bio2", "image2", true);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any());
  }

  @Test
  void should_fail_follow_when_not_authenticated() {
    setAnonymous();

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void should_fail_follow_when_user_not_found() {
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_unfollow_user_success() {
    ProfileData profileData =
        new ProfileData(targetUser.getId(), "targetuser", "bio2", "image2", false);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(
            Optional.of(new io.spring.core.user.FollowRelation(user.getId(), targetUser.getId())));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    verify(userRepository).removeRelation(any());
  }

  @Test
  void should_fail_unfollow_when_not_authenticated() {
    setAnonymous();

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void should_fail_unfollow_when_user_not_found() {
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void should_fail_unfollow_when_no_relation() {
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
