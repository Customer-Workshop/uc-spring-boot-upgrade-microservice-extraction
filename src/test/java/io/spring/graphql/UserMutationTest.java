package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();

    User newUser = new User("new@test.com", "newuser", "password123", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(newUser, result.getLocalContext());
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  void should_return_error_when_create_user_has_constraint_violations() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("invalid")
            .username("")
            .password("short")
            .build();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) RegisterParam.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("registerParam.email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("must be a valid email");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNull(result.getLocalContext());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrongpassword", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrongpassword", "test@test.com"));
  }

  @Test
  void should_throw_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@test.com"));
  }

  @Test
  void should_update_user_success() {
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@test.com")
            .bio("updated bio")
            .password("newpassword")
            .image("newimage")
            .build();

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());

    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_null_when_updating_with_anonymous_authentication() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymous", java.util.Collections.singletonList(() -> "ROLE_ANONYMOUS"));
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(anonymousToken);
    SecurityContextHolder.setContext(securityContext);

    DataFetcherResult<UserPayload> result =
        userMutation.updateUser(UpdateUserInput.newBuilder().build());

    assertNull(result);

    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_null_when_updating_with_null_principal() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(null);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    DataFetcherResult<UserPayload> result =
        userMutation.updateUser(UpdateUserInput.newBuilder().build());

    assertNull(result);

    SecurityContextHolder.clearContext();
  }
}
