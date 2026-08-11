package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
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
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder encryptService;

  @Mock private UserService userService;

  @InjectMocks private UserMutation userMutation;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "encoded", "bio", "image");
  }

  private CreateUserInput createUserInput() {
    CreateUserInput input = new CreateUserInput();
    input.setEmail(user.getEmail());
    input.setUsername(user.getUsername());
    input.setPassword("123");
    return input;
  }

  @Test
  public void should_create_user() {
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(createUserInput());

    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_return_error_data_when_registration_is_invalid() {
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException("invalid", Collections.emptySet()));

    DataFetcherResult<UserResult> result = userMutation.createUser(createUserInput());

    assertTrue(result.getData() instanceof Error);
    assertEquals("BAD_REQUEST", ((Error) result.getData()).getMessage());
    assertNull(result.getLocalContext());
  }

  @Test
  public void should_login_with_valid_credentials() {
    when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("123"), eq(user.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", user.getEmail());

    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_reject_login_with_wrong_password() {
    when(userRepository.findByEmail(eq(user.getEmail()))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq(user.getPassword()))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", user.getEmail()));
  }

  @Test
  public void should_reject_login_of_unknown_email() {
    when(userRepository.findByEmail(eq("ghost@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("123", "ghost@test.com"));
  }

  @Test
  public void should_update_current_user() {
    authenticate(user);
    UpdateUserInput input = new UpdateUserInput();
    input.setEmail("new@test.com");
    input.setUsername("newname");
    input.setBio("new bio");
    input.setPassword("new password");
    input.setImage("new image");
    ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(captor.capture());
    assertEquals("new@test.com", captor.getValue().getParam().getEmail());
    assertEquals(user, captor.getValue().getTargetUser());
  }

  @Test
  public void should_not_update_user_for_anonymous_request() {
    anonymous();

    assertNull(userMutation.updateUser(new UpdateUserInput()));
    verify(userService, never()).updateUser(any());
  }
}
