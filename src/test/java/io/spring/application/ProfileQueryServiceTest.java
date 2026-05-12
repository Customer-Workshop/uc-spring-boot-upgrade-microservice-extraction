package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileQueryServiceTest {

  @Mock private UserReadService userReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private ProfileQueryService profileQueryService;

  private User currentUser;
  private UserData userData;

  @BeforeEach
  void setUp() {
    currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
    userData = new UserData("target-id", "target@test.com", "target", "bio", "image");
  }

  @Test
  void should_find_profile_by_username() {
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("target-id")))
        .thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", currentUser);

    assertTrue(result.isPresent());
    assertEquals("target", result.get().getUsername());
    assertTrue(result.get().isFollowing());
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername(eq("nonexistent"))).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", currentUser);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_profile_without_current_user() {
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", null);

    assertTrue(result.isPresent());
    assertEquals("target", result.get().getUsername());
    assertFalse(result.get().isFollowing());
  }

  @Test
  void should_return_not_following_when_not_followed() {
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("target-id")))
        .thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", currentUser);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }
}
