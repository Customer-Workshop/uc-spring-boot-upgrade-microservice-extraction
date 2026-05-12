package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    User newUser = new User("new@test.com", "newuser", "encoded", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
  }

  @Test
  public void should_return_errors_when_create_user_with_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("existing@test.com")
            .username("existing")
            .password("password")
            .build();
    when(userService.createUser(any()))
        .thenThrow(
            new ConstraintViolationException("validation failed", new java.util.HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("123"), eq(user.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", "test@test.com");
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_throw_invalid_auth_when_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("wrong"), eq(user.getPassword()))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  public void should_throw_invalid_auth_when_user_not_found() {
    when(userRepository.findByEmail(eq("nonexistent@test.com"))).thenReturn(Optional.empty());
    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@test.com"));
  }

  @Test
  public void should_update_user_success() {
    authenticateUser(user);
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .email("updated@test.com")
            .username("updateduser")
            .bio("new bio")
            .image("new image")
            .password("newpassword")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_anonymous_update_user() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().email("a@b.com").username("u").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_is_null_update_user() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder().email("a@b.com").username("u").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }
}
