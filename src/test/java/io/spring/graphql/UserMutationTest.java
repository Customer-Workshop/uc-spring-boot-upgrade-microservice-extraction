package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserMutationTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();

  @Test
  void createsUserAndReturnsValidationErrors() {
    UserService service = mock(UserService.class);
    when(service.createUser(any())).thenReturn(user);
    UserMutation mutation = new UserMutation(mock(UserRepository.class), mock(PasswordEncoder.class), service);
    assertEquals(
        user,
        mutation
            .createUser(CreateUserInput.newBuilder().email("e").username("u").password("p").build())
            .getLocalContext());
    when(service.createUser(any())).thenThrow(new ConstraintViolationException("invalid", java.util.Collections.emptySet()));
    assertEquals(
        "BAD_REQUEST",
        ((io.spring.graphql.types.Error)
                mutation.createUser(CreateUserInput.newBuilder().build()).getData())
            .getMessage());
  }

  @Test
  void loginRequiresExistingUserAndMatchingPassword() {
    UserRepository users = mock(UserRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    UserMutation mutation = new UserMutation(users, encoder, mock(UserService.class));
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(encoder.matches("password", user.getPassword())).thenReturn(true);
    assertEquals(user, mutation.login("password", user.getEmail()).getLocalContext());
    when(encoder.matches("bad", user.getPassword())).thenReturn(false);
    assertThrows(InvalidAuthenticationException.class, () -> mutation.login("bad", user.getEmail()));
    when(users.findByEmail("missing")).thenReturn(Optional.empty());
    assertThrows(InvalidAuthenticationException.class, () -> mutation.login("password", "missing"));
  }

  @Test
  void updateUserReturnsNullWhenAnonymousAndUpdatesAuthenticatedUser() {
    UserService service = mock(UserService.class);
    UserMutation mutation = new UserMutation(mock(UserRepository.class), mock(PasswordEncoder.class), service);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", java.util.Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertNull(mutation.updateUser(UpdateUserInput.newBuilder().build()));
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    assertEquals(
        user,
        mutation
            .updateUser(UpdateUserInput.newBuilder().username("updated").build())
            .getLocalContext());
  }
}
