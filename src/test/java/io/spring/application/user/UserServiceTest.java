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
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user_successfully() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");
    when(passwordEncoder.encode("password")).thenReturn("encoded-password");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encoded-password", result.getPassword());
    assertEquals("https://default-image.png", result.getImage());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("password");
  }

  @Test
  void should_update_user_email_and_username() {
    User user = new User("old@test.com", "oldname", "password", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newname", user.getUsername());
    verify(userRepository).save(user);
  }

  @Test
  void should_update_user_bio_and_image() {
    User user = new User("test@test.com", "name", "password", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("")
            .username("")
            .password("")
            .bio("New bio")
            .image("newimage.jpg")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("New bio", user.getBio());
    assertEquals("newimage.jpg", user.getImage());
    verify(userRepository).save(user);
  }

  @Test
  void should_update_user_password() {
    User user = new User("test@test.com", "name", "oldpass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("")
            .username("")
            .password("newpass")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("newpass", user.getPassword());
    verify(userRepository).save(user);
  }

  @Test
  void should_not_change_fields_when_empty_params() {
    User user = new User("test@test.com", "name", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("").username("").password("").bio("").image("").build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("test@test.com", user.getEmail());
    assertEquals("name", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
    verify(userRepository).save(user);
  }
}
