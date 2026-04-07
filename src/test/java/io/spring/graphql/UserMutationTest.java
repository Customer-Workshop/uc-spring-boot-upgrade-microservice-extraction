package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import org.junit.jupiter.api.AfterEach;
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
    user = new User("test@test.com", "testuser", "encodedPassword", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthentication(User user) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(user, result.getLocalContext());
    verify(userService).createUser(any());
  }

  @Test
  void should_return_errors_when_create_user_with_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("existing@test.com")
            .username("existinguser")
            .password("password")
            .build();
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", new java.util.HashSet<>());
    when(userService.createUser(any())).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedPassword"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail(eq("nonexistent@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@test.com"));
  }

  @Test
  void should_update_user_successfully() {
    setAuthentication(user);
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@test.com")
            .bio("updated bio")
            .password("newpassword")
            .image("newimage")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_update_user_with_anonymous_authentication() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().username("updateduser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }

  @Test
  void should_return_null_when_update_user_with_null_principal() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().username("updateduser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }
}
