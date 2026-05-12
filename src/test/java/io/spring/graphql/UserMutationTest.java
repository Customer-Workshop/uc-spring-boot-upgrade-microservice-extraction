package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
  public void setUp() {
    user = new User("test@test.com", "testuser", "encodedPassword", "bio", "image");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();

    User newUser = new User("new@test.com", "newuser", "encoded", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
  }

  @Test
  public void should_return_error_when_create_user_with_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("password")
            .build();

    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException("validation failed", new HashSet<>()));

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);

    var result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_throw_invalid_authentication_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedPassword"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  public void should_throw_invalid_authentication_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail(eq("nonexistent@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "nonexistent@test.com"));
  }

  @Test
  public void should_update_user_success() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updated")
            .email("updated@test.com")
            .bio("new bio")
            .image("new image")
            .build();

    var result = userMutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  public void should_return_null_when_update_user_anonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder().username("updated").build();

    var result = userMutation.updateUser(input);

    assertNull(result);
  }

  @Test
  public void should_return_null_when_update_user_with_null_principal() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder().username("updated").build();

    var result = userMutation.updateUser(input);

    assertNull(result);
  }
}
