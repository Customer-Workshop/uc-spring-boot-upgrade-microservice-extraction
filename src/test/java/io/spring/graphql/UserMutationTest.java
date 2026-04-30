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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    user = new User("test@example.com", "testuser", "encoded_password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@example.com")
            .username("newuser")
            .password("password123")
            .build();

    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(UserPayload.class, result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_create_user_handle_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("bad").username("").password("").build();

    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException(new HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password123", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password123", "test@example.com");

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_login_throw_on_wrong_password() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@example.com"));
  }

  @Test
  void should_login_throw_on_unknown_email() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@example.com"));
  }

  @Test
  void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@example.com")
            .bio("new bio")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void should_update_user_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    UpdateUserInput input = UpdateUserInput.newBuilder().username("test").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }

  @Test
  void should_update_user_return_null_when_principal_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput input = UpdateUserInput.newBuilder().username("test").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }
}
