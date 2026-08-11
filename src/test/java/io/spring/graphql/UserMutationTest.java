package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.ValidationTestHelper;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  public void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("a@test.com", "a", "encoded", "bio", "image");
  }

  @Test
  public void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("a@test.com").username("a").password("123").build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    Assertions.assertTrue(result.getData() instanceof UserPayload);
    Assertions.assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_return_errors_when_create_user_is_invalid() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("").username("").password("123").build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(ValidationTestHelper.beanViolations());

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    Assertions.assertTrue(result.getData() instanceof Error);
    Assertions.assertEquals("BAD_REQUEST", ((Error) result.getData()).getMessage());
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("123"), eq(user.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", user.getEmail());

    Assertions.assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_not_login_with_wrong_password() {
    when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq(user.getPassword()))).thenReturn(false);

    Assertions.assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", user.getEmail()));
  }

  @Test
  public void should_not_login_with_unknown_email() {
    when(userRepository.findByEmail(eq("unknown@test.com"))).thenReturn(Optional.empty());

    Assertions.assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("123", "unknown@test.com"));
  }

  @Test
  public void should_update_user_success() {
    setCurrentUser(user);
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("new")
            .bio("new bio")
            .password("new password")
            .image("new image")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
    verify(userService).updateUser(captor.capture());
    Assertions.assertEquals("new@test.com", captor.getValue().getParam().getEmail());
    Assertions.assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_not_update_user_without_login() {
    setAnonymousUser();

    Assertions.assertNull(userMutation.updateUser(UpdateUserInput.newBuilder().build()));
    verify(userService, never()).updateUser(any());
  }

  @Test
  public void should_not_update_user_when_principal_is_null() {
    setNullPrincipal();

    Assertions.assertNull(userMutation.updateUser(UpdateUserInput.newBuilder().build()));
  }
}
