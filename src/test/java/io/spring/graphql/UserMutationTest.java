package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();

    User newUser = new User("new@test.com", "newuser", "encoded", "", "default-image");
    when(userService.createUser(any())).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(newUser, result.getLocalContext());
    verify(userService).createUser(any());
  }

  @Test
  void should_return_errors_when_create_user_validation_fails() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("invalid")
            .username("")
            .password("short")
            .build();

    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException(new HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNull(result.getLocalContext());
  }

  @Test
  void should_login_success() {
    User existingUser = new User("login@test.com", "loginuser", "encoded", "", "");
    when(userRepository.findByEmail("login@test.com")).thenReturn(Optional.of(existingUser));
    when(encryptService.matches("password", "encoded")).thenReturn(true);

    DataFetcherResult<UserPayload> result =
        userMutation.login("password", "login@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(existingUser, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    User existingUser = new User("login@test.com", "loginuser", "encoded", "", "");
    when(userRepository.findByEmail("login@test.com")).thenReturn(Optional.of(existingUser));
    when(encryptService.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "login@test.com"));
  }

  @Test
  void should_throw_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@test.com"));
  }

  @Test
  void should_update_user_success() {
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@test.com")
            .bio("new bio")
            .password("newpass")
            .image("new-image")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_update_user_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().username("updateduser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }

  @Test
  void should_return_null_when_update_user_null_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().username("updateduser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }

  @Test
  void should_update_user_with_partial_fields() {
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().username("updateduser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }
}
