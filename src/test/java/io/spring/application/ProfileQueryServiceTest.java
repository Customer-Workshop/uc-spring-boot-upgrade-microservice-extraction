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

  @Test
  public void should_find_profile_by_username_with_following() {
    User currentUser = new User("current@test.com", "currentuser", "pass", "", "");
    UserData userData = new UserData("targetId", "target@test.com", "targetuser", "bio", "img");

    when(userReadService.findByUsername(eq("targetuser"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("targetId")))
        .thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetuser", currentUser);

    assertTrue(result.isPresent());
    assertEquals("targetuser", result.get().getUsername());
    assertTrue(result.get().isFollowing());
  }

  @Test
  public void should_find_profile_by_username_without_following() {
    User currentUser = new User("current@test.com", "currentuser", "pass", "", "");
    UserData userData = new UserData("targetId", "target@test.com", "targetuser", "bio", "img");

    when(userReadService.findByUsername(eq("targetuser"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(eq(currentUser.getId()), eq("targetId")))
        .thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetuser", currentUser);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_find_profile_by_username_with_null_current_user() {
    UserData userData = new UserData("targetId", "target@test.com", "targetuser", "bio", "img");
    when(userReadService.findByUsername(eq("targetuser"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetuser", null);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername(eq("nonexistent"))).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", null);

    assertFalse(result.isPresent());
  }
}
