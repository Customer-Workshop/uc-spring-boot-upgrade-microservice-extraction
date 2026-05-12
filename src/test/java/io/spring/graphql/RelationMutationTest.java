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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User target;

  @BeforeEach
  void setUp() {
    user = new User("user@test.com", "user1", "password", "bio", "image");
    target = new User("target@test.com", "target1", "password", "bio2", "image2");
  }

  @Test
  void should_follow_user_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));

      ProfileData profileData = new ProfileData(target.getId(), "target1", "bio2", "image2", true);
      when(profileQueryService.findByUsername(eq("target1"), eq(user)))
          .thenReturn(Optional.of(profileData));

      ProfilePayload result = relationMutation.follow("target1");

      assertNotNull(result);
      assertNotNull(result.getProfile());
      assertEquals("target1", result.getProfile().getUsername());
      verify(userRepository).saveRelation(any(FollowRelation.class));
    }
  }

  @Test
  void should_throw_authentication_when_following_without_login() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> relationMutation.follow("target1"));
    }
  }

  @Test
  void should_throw_not_found_when_following_nonexistent_user() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
    }
  }

  @Test
  void should_unfollow_user_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));

      FollowRelation relation = new FollowRelation(user.getId(), target.getId());
      when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
          .thenReturn(Optional.of(relation));

      ProfileData profileData = new ProfileData(target.getId(), "target1", "bio2", "image2", false);
      when(profileQueryService.findByUsername(eq("target1"), eq(user)))
          .thenReturn(Optional.of(profileData));

      ProfilePayload result = relationMutation.unfollow("target1");

      assertNotNull(result);
      assertNotNull(result.getProfile());
      assertEquals("target1", result.getProfile().getUsername());
      verify(userRepository).removeRelation(eq(relation));
    }
  }

  @Test
  void should_throw_authentication_when_unfollowing_without_login() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("target1"));
    }
  }

  @Test
  void should_throw_not_found_when_unfollowing_nonexistent_user() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
    }
  }

  @Test
  void should_throw_not_found_when_relation_not_found_on_unfollow() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(userRepository.findByUsername(eq("target1"))).thenReturn(Optional.of(target));
      when(userRepository.findRelation(eq(user.getId()), eq(target.getId())))
          .thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("target1"));
    }
  }
}
