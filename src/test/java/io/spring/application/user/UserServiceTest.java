package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

  private UserService userService;
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    when(passwordEncoder.encode(any())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user_successfully() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encoded_password", result.getPassword());
    assertEquals("https://default-image.png", result.getImage());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("password");
  }

  @Test
  void should_create_user_with_default_image() {
    RegisterParam param = new RegisterParam("user@test.com", "user1", "pass123");

    User result = userService.createUser(param);

    assertEquals("https://default-image.png", result.getImage());
  }

  @Test
  void should_update_user_successfully() {
    User existingUser = new User("old@example.com", "olduser", "oldpass", "old bio", "old image");

    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    verify(userRepository).save(existingUser);
    assertEquals("new@example.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
  }

  @Test
  void should_update_user_with_empty_fields() {
    User existingUser = new User("old@example.com", "olduser", "oldpass", "bio", "image");

    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("")
            .username("")
            .password("")
            .bio("")
            .image("")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    verify(userRepository).save(existingUser);
  }
}
