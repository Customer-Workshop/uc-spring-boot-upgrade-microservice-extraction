package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserQueryServiceTest {

  private UserReadService userReadService;
  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    userReadService = mock(UserReadService.class);
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_find_user_by_id() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    when(userReadService.findById("id")).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("id");

    assertTrue(result.isPresent());
    assertEquals("username", result.get().getUsername());
    assertEquals("email@test.com", result.get().getEmail());
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findById("nonexistent")).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("nonexistent");

    assertFalse(result.isPresent());
  }
}
