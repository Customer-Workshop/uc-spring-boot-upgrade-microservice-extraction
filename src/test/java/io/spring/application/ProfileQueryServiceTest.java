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

  @BeforeEach
  void setUp() {
    currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_find_profile_by_username() {
    UserData userData = new UserData("uid", "target@test.com", "targetuser", "bio", "img");
    when(userReadService.findByUsername(eq("targetuser"))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(currentUser.getId(), "uid")).thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetuser", currentUser);

    assertTrue(result.isPresent());
    assertEquals("targetuser", result.get().getUsername());
    assertTrue(result.get().isFollowing());
  }

  @Test
  void should_return_not_following_when_no_current_user() {
    UserData userData = new UserData("uid", "target@test.com", "targetuser", "bio", "img");
    when(userReadService.findByUsername(eq("targetuser"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetuser", null);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername(eq("nonexistent"))).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", currentUser);

    assertFalse(result.isPresent());
  }
}
