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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_get_me_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

    UserData userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Bearer mytoken", dfe);
    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  public void should_return_null_when_anonymous() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Bearer token", dfe);
    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Bearer token", dfe);
    assertNull(result);
  }

  @Test
  public void should_throw_when_user_not_found() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    assertThrows(
        ResourceNotFoundException.class,
        () -> meDatafetcher.getMe("Bearer token", dfe));
  }

  @Test
  public void should_get_user_payload_user() {
    when(jwtService.toToken(eq(user))).thenReturn("generated-token");

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dfe);
    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("generated-token", result.getData().getToken());
  }
}
