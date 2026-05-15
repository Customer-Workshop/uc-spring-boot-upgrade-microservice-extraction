package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfileQueryServiceTest2 {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    userReadService = mock(UserReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
  }

  @Test
  public void should_find_profile_by_username() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    when(userReadService.findByUsername("username")).thenReturn(userData);
    User currentUser = new User("email@test.com", "currentUser", "pass", "", "");
    when(userRelationshipQueryService.isUserFollowing(currentUser.getId(), "id")).thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("username", currentUser);

    assertTrue(result.isPresent());
    assertEquals("username", result.get().getUsername());
    assertEquals("bio", result.get().getBio());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_return_empty_when_username_not_found() {
    when(userReadService.findByUsername("nonexistent")).thenReturn(null);
    User currentUser = new User("email@test.com", "currentUser", "pass", "", "");

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", currentUser);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_show_following_true_when_user_follows() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    when(userReadService.findByUsername("username")).thenReturn(userData);
    User currentUser = new User("email@test.com", "currentUser", "pass", "", "");
    when(userRelationshipQueryService.isUserFollowing(currentUser.getId(), "id")).thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("username", currentUser);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFollowing());
  }

  @Test
  public void should_handle_null_current_user() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    when(userReadService.findByUsername("username")).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("username", null);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }
}
