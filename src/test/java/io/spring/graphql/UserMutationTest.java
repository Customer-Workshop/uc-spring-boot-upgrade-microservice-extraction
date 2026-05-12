package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(u, null));
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();
    User newUser = new User("new@test.com", "newuser", "encoded", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(newUser);

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(newUser, result.getLocalContext());
  }

  @Test
  void should_return_error_when_create_user_has_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("duplicate@test.com")
            .username("dup")
            .password("pass")
            .build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(
            new ConstraintViolationException("validation failed", new java.util.HashSet<>()));

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("password"))).thenReturn(true);

    var result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_invalid_authentication_on_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("password"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_invalid_authentication_on_unknown_email() {
    when(userRepository.findByEmail(eq("unknown@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  void should_update_user_success() {
    setAuthenticated(user);
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("updated@test.com")
            .username("updated")
            .bio("new bio")
            .image("new image")
            .password("newpass")
            .build();

    var result = userMutation.updateUser(input);

    assertNotNull(result);
    assertTrue(result.getData() instanceof UserPayload);
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  void should_return_null_when_update_user_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    UpdateUserInput input = UpdateUserInput.newBuilder().email("updated@test.com").build();

    var result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }

  @Test
  void should_return_null_when_update_user_null_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput input = UpdateUserInput.newBuilder().email("updated@test.com").build();

    var result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }
}
