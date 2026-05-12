package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private org.springframework.security.crypto.password.PasswordEncoder encryptService;
  @Mock private UserService userService;

  @InjectMocks private UserMutation userMutation;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "encodedpw", "bio", "image");
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_success() {
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  void should_return_error_when_constraint_violation_on_create() {
    ConstraintViolationException cve = new ConstraintViolationException(new HashSet<>());
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("password")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNull(result.getLocalContext());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedpw"))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_invalid_auth_when_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedpw"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_invalid_auth_when_user_not_found() {
    when(userRepository.findByEmail(eq("unknown@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  void should_update_user_success() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .image("new image")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_anonymous_user_updates() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }

  @Test
  void should_return_null_when_null_principal_on_update() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }
}
