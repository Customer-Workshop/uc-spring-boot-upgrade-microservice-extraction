package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.UserPayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class MeDatafetcherTest {

  private MeDatafetcher meDatafetcher;
  private UserQueryService userQueryService;
  private JwtService jwtService;
  private User user;

  @BeforeEach
  void setUp() {
    userQueryService = mock(UserQueryService.class);
    jwtService = mock(JwtService.class);
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_me_when_authenticated() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UserData userData = new UserData(user.getId(), "test@example.com", "testuser", "bio", "image");
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token jwt-token-123", dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@example.com", result.getData().getEmail());
    assertEquals("jwt-token-123", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    AnonymousAuthenticationToken token =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(token);

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token jwt-token-123", dfe);

    assertNull(result);
  }

  @Test
  void should_get_user_payload_user() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("generated-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@example.com", result.getData().getEmail());
    assertEquals("generated-token", result.getData().getToken());
  }
}
