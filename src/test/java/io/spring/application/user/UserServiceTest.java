package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user_success() {
    when(passwordEncoder.encode(eq("password123"))).thenReturn("encoded-password");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password123");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encoded-password", result.getPassword());
    assertEquals("https://default-image.png", result.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user_success() {
    User existingUser = new User("old@test.com", "olduser", "oldpass", "old bio", "old.png");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@test.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_update_user_with_partial_fields() {
    User existingUser = new User("old@test.com", "olduser", "oldpass", "old bio", "old.png");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@test.com", existingUser.getEmail());
    assertEquals("olduser", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_create_register_param() {
    RegisterParam param = new RegisterParam("email@test.com", "user", "pass");
    assertEquals("email@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
  }

  @Test
  void should_create_update_user_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@test.com")
            .username("user")
            .bio("bio")
            .image("img")
            .password("pass")
            .build();
    assertEquals("test@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
    assertEquals("pass", param.getPassword());
  }

  @Test
  void should_create_update_user_command() {
    User user = new User("test@test.com", "user", "pass", "", "");
    UpdateUserParam param = UpdateUserParam.builder().email("new@test.com").build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }

  @Test
  void should_validate_duplicated_email() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();

    assertTrue(validator.isValid(null, null));
    assertTrue(validator.isValid("", null));
  }

  @Test
  void should_validate_duplicated_username() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();

    assertTrue(validator.isValid(null, null));
    assertTrue(validator.isValid("", null));
  }
}
