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
    userService =
        new UserService(userRepository, "https://default-image.com/avatar.png", passwordEncoder);
  }

  @Test
  void should_create_user_success() {
    RegisterParam param = new RegisterParam("new@test.com", "newuser", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("new@test.com", result.getEmail());
    assertEquals("newuser", result.getUsername());
    assertEquals("encoded_password", result.getPassword());
    assertEquals("https://default-image.com/avatar.png", result.getImage());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("password123");
  }

  @Test
  void should_update_user_email() {
    User existingUser = new User("old@test.com", "user", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("new@test.com", existingUser.getEmail());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_update_user_username() {
    User existingUser = new User("test@test.com", "oldname", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("")
            .username("newname")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("newname", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_update_user_bio_and_image() {
    User existingUser = new User("test@test.com", "user", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("")
            .username("")
            .password("")
            .bio("new bio")
            .image("new-image.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("new bio", existingUser.getBio());
    assertEquals("new-image.png", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_not_change_user_with_empty_params() {
    User existingUser = new User("test@test.com", "user", "pass", "bio", "img");
    UpdateUserParam param =
        UpdateUserParam.builder().email("").username("").password("").bio("").image("").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("test@test.com", existingUser.getEmail());
    assertEquals("user", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }
}
