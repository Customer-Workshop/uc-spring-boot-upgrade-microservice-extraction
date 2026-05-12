package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

  @InjectMocks private UserMutation userMutation;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "encoded_password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(UserPayload.class, result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_return_errors_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("password")
            .build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(new java.util.HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_on_invalid_login_email() {
    when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "wrong@test.com"));
  }

  @Test
  void should_throw_on_invalid_login_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrongpass", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrongpass", "test@test.com"));
  }

  @Test
  void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("updated")
            .email("updated@test.com")
            .bio("new bio")
            .password("newpass")
            .image("newimage")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_anonymous_updates_user() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("x").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }

  @Test
  void should_return_null_when_null_principal_updates_user() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("x").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }
}
