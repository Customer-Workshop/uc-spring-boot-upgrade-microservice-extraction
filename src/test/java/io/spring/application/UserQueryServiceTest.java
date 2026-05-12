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
    UserData userData = new UserData("id", "email@test.com", "user", "bio", "img");
    when(userReadService.findById(eq("id"))).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("id");

    assertTrue(result.isPresent());
    assertEquals("email@test.com", result.get().getEmail());
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findById(eq("missing"))).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("missing");

    assertFalse(result.isPresent());
  }
}
