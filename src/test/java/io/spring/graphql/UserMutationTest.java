package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
  public void setUp() {
    user = new User("test@test.com", "testuser", "encoded123", "bio", "image");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User user) {
    TestingAuthenticationToken authToken = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  @Test
  public void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("123")
            .build();

    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_return_errors_when_constraint_violation_on_create() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("bad-email")
            .username("testuser")
            .password("123")
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
    when(encryptService.matches(eq("123"), eq(user.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", "test@test.com");
    assertNotNull(result);
    assertNotNull(result.getLocalContext());
  }

  @Test
  public void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq(user.getPassword()))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  public void should_throw_when_login_user_not_found() {
    when(userRepository.findByEmail(eq("notfound@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("123", "notfound@test.com"));
  }

  @Test
  public void should_update_user_success() {
    setAuthenticated(user);
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_anonymous_update_user() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_is_null_update_user() {
    TestingAuthenticationToken authToken = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(authToken);

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }
}
