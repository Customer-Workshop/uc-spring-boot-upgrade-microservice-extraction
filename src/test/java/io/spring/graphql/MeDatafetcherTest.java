package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;
  private UserData userData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_me_when_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token mytoken", dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token token", dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token token", dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void should_get_user_payload_user() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("jwt-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("jwt-token", result.getData().getToken());
  }
}
