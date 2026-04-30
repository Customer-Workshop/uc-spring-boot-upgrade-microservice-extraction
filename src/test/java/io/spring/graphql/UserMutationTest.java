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
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest {

  private UserRepository userRepository;
  private PasswordEncoder encryptService;
  private UserService userService;
  private UserMutation userMutation;
  private User user;

  @BeforeEach
  public void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    encryptService = Mockito.mock(PasswordEncoder.class);
    userService = Mockito.mock(UserService.class);
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "encodedpass", "", "");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user_success() {
    User newUser = new User("new@test.com", "newuser", "encodedpass", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertEquals(newUser, result.getLocalContext());
  }

  @Test
  public void should_return_errors_when_create_user_validation_fails() {
    ConstraintViolationException cve = new ConstraintViolationException(new HashSet<>());
    when(userService.createUser(any())).thenThrow(cve);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("pass")
            .build();
    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNull(result.getLocalContext());
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedpass"))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedpass"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  public void should_throw_when_login_with_unknown_email() {
    when(userRepository.findByEmail(eq("unknown@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  public void should_update_user() {
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("updated")
            .email("updated@test.com")
            .bio("my bio")
            .image("http://img.com/pic.png")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_anonymous_on_update() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("u").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_null_on_update() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("u").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);
    assertNull(result);
  }
}
