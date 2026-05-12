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
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
    user = new User("test@test.com", "testuser", "encodedpass", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void loginAs(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  void should_create_user_successfully() {
    when(userService.createUser(any())).thenReturn(user);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("pass")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_return_error_when_create_user_has_constraint_violations() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("duplicated email");
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation =
        Mockito.mock(javax.validation.constraints.NotBlank.class);
    when(annotation.annotationType())
        .thenReturn((Class) javax.validation.constraints.NotBlank.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    Set violations = new HashSet();
    violations.add(violation);
    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException("error", violations));

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("pass")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", "encodedpass")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("pass", "test@test.com");
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_throw_invalid_auth_when_login_with_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encodedpass")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_invalid_auth_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("pass", "nonexistent@test.com"));
  }

  @Test
  void should_update_user_successfully() {
    loginAs(user);

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
  void should_return_null_when_update_user_without_auth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }

  @Test
  void should_return_null_when_update_user_with_null_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }
}
