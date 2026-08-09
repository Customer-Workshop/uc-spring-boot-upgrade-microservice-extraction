package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class MeDatafetcherTest extends GraphqlTestBase {
  private final io.spring.core.user.User currentUser = GraphqlTestFixtures.user();

  @Test
  void returnsNullForAnonymousUser() {
    MeDatafetcher fetcher = new MeDatafetcher(mock(UserQueryService.class), mock(JwtService.class));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertNull(fetcher.getMe("Token token", mock(graphql.schema.DataFetchingEnvironment.class)));
  }

  @Test
  void getsCurrentUserAndTokenOrNotFound() {
    UserQueryService users = mock(UserQueryService.class);
    JwtService jwt = mock(JwtService.class);
    UserData data =
        new UserData(
            currentUser.getId(),
            currentUser.getEmail(),
            currentUser.getUsername(),
            currentUser.getBio(),
            currentUser.getImage());
    when(users.findById(currentUser.getId())).thenReturn(Optional.of(data));
    when(jwt.toToken(currentUser)).thenReturn("jwt");
    MeDatafetcher fetcher = new MeDatafetcher(users, jwt);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null));
    assertEquals(
        currentUser.getUsername(),
        fetcher
            .getMe("Token jwt", mock(graphql.schema.DataFetchingEnvironment.class))
            .getData()
            .getUsername());
    assertEquals(
        currentUser.getUsername(),
        fetcher
            .getUserPayloadUser(GraphqlTestFixtures.environment(currentUser))
            .getData()
            .getUsername());
    when(users.findById(currentUser.getId())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> fetcher.getMe("Token jwt", mock(graphql.schema.DataFetchingEnvironment.class)));
  }
}
