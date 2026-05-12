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
    user = new User("test@test.com", "testuser", "encoded-password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .build();
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(UserPayload.class, result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_return_error_when_user_creation_violates_constraints() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("bad").username("").password("").build();
    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException("validation failed", new HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(io.spring.graphql.types.Error.class, result.getData());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encoded-password"))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encoded-password"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail(eq("nonexistent@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("pass", "nonexistent@test.com"));
  }

  @Test
  void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .image("new image")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_updating_user_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }
}
