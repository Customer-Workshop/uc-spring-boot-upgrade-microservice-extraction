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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation mutation;
  private User user;

  @BeforeEach
  void setUp() {
    mutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("pass")
            .build();

    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertInstanceOf(UserPayload.class, result.getData());
  }

  @Test
  void should_return_error_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("pass")
            .build();

    ConstraintViolationException cve = mock(ConstraintViolationException.class);
    when(cve.getConstraintViolations()).thenReturn(java.util.Collections.emptySet());
    when(userService.createUser(any())).thenThrow(cve);

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertNotNull(result);
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password", "password")).thenReturn(true);

    DataFetcherResult<UserPayload> result = mutation.login("password", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getLocalContext());
  }

  @Test
  void should_throw_on_login_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", "password")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> mutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_on_login_user_not_found() {
    when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> mutation.login("pass", "missing@test.com"));
  }

  @Test
  void should_update_user() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("updated@test.com")
            .username("updated")
            .bio("new bio")
            .image("new image")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_update_user_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    UpdateUserInput input = UpdateUserInput.newBuilder().email("e@e.com").build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    assertNull(result);
  }
}
