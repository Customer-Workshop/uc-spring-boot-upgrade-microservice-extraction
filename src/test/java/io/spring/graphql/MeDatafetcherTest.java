package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.graphql.types.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dfe;

  private MeDatafetcher meDatafetcher;
  private io.spring.core.user.User coreUser;

  @BeforeEach
  void setUp() {
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    coreUser = new io.spring.core.user.User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_me_when_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(coreUser, null));

    UserData userData =
        new UserData(coreUser.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(eq(coreUser.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<User> result = meDatafetcher.getMe("Token mytoken", dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    DataFetcherResult<User> result = meDatafetcher.getMe("Token x", dfe);

    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<User> result = meDatafetcher.getMe("Token x", dfe);

    assertNull(result);
  }

  @Test
  void should_get_user_payload_user() {
    when(dfe.getLocalContext()).thenReturn(coreUser);
    when(jwtService.toToken(eq(coreUser))).thenReturn("jwt-token");

    DataFetcherResult<User> result = meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("jwt-token", result.getData().getToken());
  }
}
