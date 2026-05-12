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
  private User currentUser;

  @BeforeEach
  public void setUp() {
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
    currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_find_profile_by_username_with_following() {
    UserData userData = new UserData("target-id", "target@test.com", "target", "bio", "image");
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("target-id")))
        .thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", currentUser);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFollowing());
    assertEquals("target", result.get().getUsername());
  }

  @Test
  public void should_find_profile_by_username_without_following() {
    UserData userData = new UserData("target-id", "target@test.com", "target", "bio", "image");
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("target-id")))
        .thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", currentUser);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_find_profile_without_current_user() {
    UserData userData = new UserData("target-id", "target@test.com", "target", "bio", "image");
    when(userReadService.findByUsername(eq("target"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("target", null);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername(eq("nonexistent"))).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", currentUser);

    assertFalse(result.isPresent());
  }
}
