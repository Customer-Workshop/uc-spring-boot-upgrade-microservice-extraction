package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  private static final String DEFAULT_IMAGE = "https://static.productionready.io/smiley.jpg";

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    userService = new UserService(userRepository, DEFAULT_IMAGE, passwordEncoder);
  }

  @Test
  public void should_create_user_with_encoded_password_and_default_image() {
    when(passwordEncoder.encode(eq("123"))).thenReturn("encoded");

    User user = userService.createUser(new RegisterParam("john@jacob.com", "johnjacob", "123"));

    assertEquals("encoded", user.getPassword());
    assertEquals(DEFAULT_IMAGE, user.getImage());
    verify(userRepository).save(user);
  }

  @Test
  public void should_update_user_fields() {
    User user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .bio("new bio")
            .image("new image")
            .password("new password")
            .build();

    userService.updateUser(new UpdateUserCommand(user, param));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals("new@test.com", captor.getValue().getEmail());
    assertEquals("newname", captor.getValue().getUsername());
    assertEquals("new bio", captor.getValue().getBio());
    assertEquals("new image", captor.getValue().getImage());
  }

  @Test
  public void should_keep_current_values_when_update_param_is_empty() {
    User user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");

    userService.updateUser(new UpdateUserCommand(user, UpdateUserParam.builder().build()));

    assertEquals("john@jacob.com", user.getEmail());
    assertEquals("johnjacob", user.getUsername());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }
}
