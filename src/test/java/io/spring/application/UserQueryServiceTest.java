package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_find_user_by_id() {
    UserData userData = new UserData("userId", "test@test.com", "testuser", "bio", "image");
    when(userReadService.findById(eq("userId"))).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("userId");
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findById(eq("nonexistent"))).thenReturn(null);
    Optional<UserData> result = userQueryService.findById("nonexistent");
    assertFalse(result.isPresent());
  }
}
