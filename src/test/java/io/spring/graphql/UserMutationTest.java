package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
    verify(userService).createUser(any(RegisterParam.class));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolationException buildConstraintViolationException() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);
    doReturn("param.email").when(propertyPath).toString();
    doReturn(propertyPath).when(violation).getPropertyPath();
    doReturn("should be an email").when(violation).getMessage();
    doReturn(String.class).when(violation).getRootBeanClass();

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    javax.validation.constraints.Email emailAnn = mock(javax.validation.constraints.Email.class);
    doReturn(javax.validation.constraints.Email.class).when(emailAnn).annotationType();
    doReturn(emailAnn).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    return new ConstraintViolationException(violations);
  }

  @Test
  void should_return_error_data_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("invalid").username("u").password("").build();

    ConstraintViolationException cve = buildConstraintViolationException();
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof io.spring.graphql.types.Error);
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password", "password")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_invalid_auth_on_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", "password")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_invalid_auth_on_nonexistent_email() {
    when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "unknown@test.com"));
  }

  @Test
  void should_update_user_when_authenticated() {
    setAuthenticated(user);
    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .email("newemail@test.com")
            .username("newname")
            .bio("new bio")
            .image("newimage.jpg")
            .password("newpassword")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_updating_user_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    UpdateUserInput updateInput = UpdateUserInput.newBuilder().email("new@test.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNull(result);
  }
}
