package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserMutationTest {

  private UserMutation userMutation;
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;
  private User user;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = mock(UserService.class);
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@example.com")
            .username("newuser")
            .password("password")
            .build();

    User newUser = new User("new@example.com", "newuser", "password", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void should_return_errors_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("duplicate@example.com")
            .username("dupuser")
            .password("password")
            .build();

    // Build all mocks first, before any stubbing
    ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
    Path path = Mockito.mock(Path.class);
    ConstraintDescriptor descriptor = Mockito.mock(ConstraintDescriptor.class);
    Annotation annotation = Mockito.mock(Annotation.class);

    // Stub leaf mocks first, then compose
    Mockito.doReturn((Class) Override.class).when(annotation).annotationType();
    Mockito.doReturn(annotation).when(descriptor).getAnnotation();
    Mockito.doReturn("param.email").when(path).toString();
    Mockito.doReturn(path).when(violation).getPropertyPath();
    Mockito.doReturn("duplicated email").when(violation).getMessage();
    Mockito.doReturn(String.class).when(violation).getRootBeanClass();
    Mockito.doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Mockito.doThrow(cve).when(userService).createUser(any(RegisterParam.class));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    // Should have error data, not user payload
    assertNotNull(result.getData());
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result =
        userMutation.login("password", "test@example.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@example.com"));
  }

  @Test
  void should_throw_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@example.com"));
  }

  @Test
  void should_update_user_successfully() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@example.com")
            .bio("new bio")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  void should_return_null_when_update_user_anonymous() {
    AnonymousAuthenticationToken token =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(token);

    UpdateUserInput input =
        UpdateUserInput.newBuilder().username("test").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }
}
