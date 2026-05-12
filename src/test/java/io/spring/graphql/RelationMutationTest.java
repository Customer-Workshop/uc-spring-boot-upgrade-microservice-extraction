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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User user;
  private User target;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "", "");
    target = new User("target@test.com", "targetuser", "123", "bio", "image");
    relationMutation = new RelationMutation(userRepository, profileQueryService);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_follow_user_success() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_throw_not_found_when_follow_nonexistent_user() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  public void should_throw_auth_when_not_logged_in_follow() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  public void should_unfollow_user_success() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");
    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_throw_not_found_when_unfollow_nonexistent_user() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  public void should_throw_not_found_when_no_relation_exists_unfollow() {
    authenticateUser(user);
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
        .thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  public void should_throw_auth_when_not_logged_in_unfollow() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
