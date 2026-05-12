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
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    user = new User("test@test.com", "testuser", "encodedpw", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user_success() {
    when(userService.createUser(any())).thenReturn(user);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_return_errors_when_create_user_validation_fails() {
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", new HashSet<>());
    when(userService.createUser(any())).thenThrow(cve);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNull(result.getLocalContext());
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedpw"))).thenReturn(true);

    DataFetcherResult<UserPayload> result =
        userMutation.login("password", "test@test.com");
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_throw_when_login_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedpw"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  public void should_throw_when_login_user_not_found() {
    when(userRepository.findByEmail(eq("unknown@test.com"))).thenReturn(Optional.empty());
    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  public void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("newname")
            .email("new@test.com")
            .bio("new bio")
            .image("new image")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_update_user_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput input =
        UpdateUserInput.newBuilder().username("newname").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_update_user_null_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    UpdateUserInput input =
        UpdateUserInput.newBuilder().username("newname").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }
}
