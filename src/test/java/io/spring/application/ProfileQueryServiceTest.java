package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileQueryServiceTest {

  @Mock private UserReadService userReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
  }

  @Test
  public void should_find_profile_by_username() {
    UserData userData = new UserData("userId", "test@test.com", "testuser", "bio", "image");
    when(userReadService.findByUsername(eq("testuser"))).thenReturn(userData);

    User currentUser = new User("current@test.com", "current", "123", "", "");
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("userId")))
        .thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
    assertTrue(result.get().isFollowing());
  }

  @Test
  public void should_find_profile_without_current_user() {
    UserData userData = new UserData("userId", "test@test.com", "testuser", "bio", "image");
    when(userReadService.findByUsername(eq("testuser"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", null);
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername(eq("nonexistent"))).thenReturn(null);
    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", null);
    assertFalse(result.isPresent());
  }

  @Test
  public void should_not_following_when_not_following() {
    UserData userData = new UserData("userId", "test@test.com", "testuser", "bio", "image");
    when(userReadService.findByUsername(eq("testuser"))).thenReturn(userData);

    User currentUser = new User("current@test.com", "current", "123", "", "");
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("userId")))
        .thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);
    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }
}
