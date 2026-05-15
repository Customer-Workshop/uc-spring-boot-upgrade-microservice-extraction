package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user_with_encoded_password() {
    when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");

    RegisterParam param = new RegisterParam("test@test.com", "testuser", "rawpassword");
    User user = userService.createUser(param);

    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("encodedpassword", user.getPassword());
    assertEquals("default-image.png", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user() {
    User existingUser = new User("old@test.com", "olduser", "oldpass", "oldbio", "oldimg");
    UpdateUserParam updateParam = new UpdateUserParam("new@test.com", "", "", "", "");
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@test.com", existingUser.getEmail());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_save_user_after_create() {
    when(passwordEncoder.encode(any())).thenReturn("encoded");

    RegisterParam param = new RegisterParam("a@b.com", "user", "pass");
    userService.createUser(param);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals("a@b.com", captor.getValue().getEmail());
  }
}
