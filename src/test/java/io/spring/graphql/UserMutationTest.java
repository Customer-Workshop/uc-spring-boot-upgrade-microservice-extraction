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
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();

    User newUser = new User("new@test.com", "newuser", "encoded", "", "default-image");
    when(userService.createUser(any())).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(newUser, result.getLocalContext());
    verify(userService).createUser(any());
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_create_user_return_errors_on_validation_failure() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("pass")
            .build();

    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    javax.validation.Path path = mock(javax.validation.Path.class);
    when(path.toString()).thenReturn("createUser.registerParam.email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("email already exists");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException("validation failed", violations));

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
  void should_login_throw_on_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_login_throw_on_unknown_email() {
    when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  void should_update_user_success() {
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("newname")
            .email("newemail@test.com")
            .bio("new bio")
            .password("newpass")
            .image("newimage")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void should_update_user_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("newname").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }

  @Test
  void should_update_user_return_null_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().username("newname").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }
}
