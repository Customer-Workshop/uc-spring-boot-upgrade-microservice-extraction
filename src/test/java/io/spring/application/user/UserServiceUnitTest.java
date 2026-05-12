package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceUnitTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  public void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  public void should_create_user() {
    when(passwordEncoder.encode("password")).thenReturn("encoded-password");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    User user = userService.createUser(param);
    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user() {
    User user = new User("old@test.com", "olduser", "pass", "old bio", "old.jpg");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.jpg")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);
    verify(userRepository).save(user);
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
  }

  @Test
  public void should_update_user_with_partial_fields() {
    User user = new User("old@test.com", "olduser", "pass", "old bio", "old.jpg");
    UpdateUserParam updateParam =
        UpdateUserParam.builder().email("new@test.com").username("").build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);
    verify(userRepository).save(user);
  }
}
