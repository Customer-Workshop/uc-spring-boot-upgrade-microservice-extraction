package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, "https://default-avatar.png", passwordEncoder);
  }

  @Test
  void should_create_user_success() {
    when(passwordEncoder.encode("password")).thenReturn("encoded");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encoded", result.getPassword());
    assertEquals("https://default-avatar.png", result.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user_success() {
    User user = new User("old@test.com", "olduser", "oldpass", "old bio", "old.png");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    verify(userRepository).save(eq(user));
  }

  @Test
  void should_update_user_with_empty_params() {
    User user = new User("old@test.com", "olduser", "oldpass", "old bio", "old.png");
    UpdateUserParam param = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("old@test.com", user.getEmail());
    assertEquals("olduser", user.getUsername());
    verify(userRepository).save(eq(user));
  }
}
