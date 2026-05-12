package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, "https://default.image.png", passwordEncoder);
  }

  @Test
  void should_create_user() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encoded");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("https://default.image.png", result.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user() {
    User existingUser = new User("old@test.com", "olduser", "pass", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@test.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("new image", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_update_user_with_defaults() {
    User existingUser = new User("old@test.com", "olduser", "pass", "bio", "image");
    UpdateUserParam updateParam = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    verify(userRepository).save(existingUser);
  }
}
