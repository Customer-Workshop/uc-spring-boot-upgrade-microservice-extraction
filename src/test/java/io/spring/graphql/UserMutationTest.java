package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    user = new User("test@example.com", "tester", "123", "", "");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@example.com")
            .username("tester")
            .password("123")
            .build();
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
    assertTrue(result.getLocalContext() instanceof User);
  }

  @Test
  public void should_return_error_data_when_create_user_violates_constraints() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("bad").username("").password("").build();
    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException(Collections.emptySet()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof Error);
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("123", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", "test@example.com");

    assertNotNull(result.getData());
    assertTrue(result.getLocalContext() instanceof User);
  }

  @Test
  public void should_throw_when_login_user_not_found() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("123", "missing@example.com"));
  }

  @Test
  public void should_throw_when_login_password_wrong() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@example.com"));
  }

  @Test
  public void should_update_user() {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@example.com")
            .username("newname")
            .bio("bio")
            .password("newpass")
            .image("img")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result.getData());
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_update_user_anonymous() {
    AnonymousAuthenticationToken authentication =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@example.com").build();

    assertNull(userMutation.updateUser(input));
  }
}
