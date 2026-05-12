package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  @InjectMocks private UserQueryService userQueryService;

  @Test
  void should_find_user_by_id() {
    UserData userData = new UserData("user-id", "test@test.com", "testuser", "bio", "image");
    when(userReadService.findById(eq("user-id"))).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("user-id");

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findById(eq("non-existent"))).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("non-existent");

    assertFalse(result.isPresent());
  }
}
