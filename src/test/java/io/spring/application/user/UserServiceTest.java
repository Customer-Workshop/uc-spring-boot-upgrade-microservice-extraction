package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
  public void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  public void should_create_user() {
    when(passwordEncoder.encode("password")).thenReturn("encoded-password");
    RegisterParam param = new RegisterParam("email@test.com", "username", "password");

    User user = userService.createUser(param);

    assertNotNull(user);
    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("encoded-password", user.getPassword());
    assertEquals("default-image.png", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user() {
    User user = new User("old@test.com", "olduser", "oldpass", "oldbio", "oldimage");
    UpdateUserParam param =
        new UpdateUserParam("new@test.com", "newpass", "newuser", "newbio", "newimage");
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimage", user.getImage());
    verify(userRepository).save(user);
  }

  @Test
  public void should_encode_password_on_create() {
    when(passwordEncoder.encode("mypassword")).thenReturn("hashed-pw");
    RegisterParam param = new RegisterParam("email@test.com", "user", "mypassword");

    User user = userService.createUser(param);

    assertEquals("hashed-pw", user.getPassword());
    verify(passwordEncoder).encode("mypassword");
  }
}
