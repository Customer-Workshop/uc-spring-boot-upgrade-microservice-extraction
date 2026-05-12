package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  private MeDatafetcher meDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_get_me_success() {
    authenticateUser(user);
    UserData userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token mytoken", dfe);
    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  public void should_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token mytoken", dfe);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_is_null() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token mytoken", dfe);
    assertNull(result);
  }

  @Test
  public void should_throw_not_found_when_user_data_not_exists() {
    authenticateUser(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    assertThrows(ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token mytoken", dfe));
  }

  @Test
  public void should_get_user_payload_user() {
    User payloadUser = new User("payload@test.com", "payloaduser", "123", "bio", "img");
    when(jwtService.toToken(eq(payloadUser))).thenReturn("jwt-token");

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(payloadUser);

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getUserPayloadUser(dfe);
    assertNotNull(result);
    assertEquals("payload@test.com", result.getData().getEmail());
    assertEquals("payloaduser", result.getData().getUsername());
    assertEquals("jwt-token", result.getData().getToken());
    assertEquals(payloadUser, result.getLocalContext());
  }
}
