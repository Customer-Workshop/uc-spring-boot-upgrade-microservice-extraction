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
  public void setUp() {
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  public void should_create_user_success() {
    when(passwordEncoder.encode(eq("password"))).thenReturn("encoded");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");
    User user = userService.createUser(param);
    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user_success() {
    User user = new User("old@test.com", "olduser", "oldpass", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpassword")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    verify(userRepository).save(user);
  }
}
