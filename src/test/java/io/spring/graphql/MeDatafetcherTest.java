package io.spring.graphql;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest extends GraphQLTestBase {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;

  private MeDatafetcher meDatafetcher;
  private User user;
  private UserData userData;

  @BeforeEach
  public void setUp() {
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    user = new User("a@test.com", "a", "123", "bio", "image");
    userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), "bio", "image");
  }

  @Test
  public void should_get_me_success() {
    setCurrentUser(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(AUTHORIZATION, mock(DataFetchingEnvironment.class));

    Assertions.assertEquals(user.getEmail(), result.getData().getEmail());
    Assertions.assertEquals(user.getUsername(), result.getData().getUsername());
    Assertions.assertEquals("jwt-token", result.getData().getToken());
    Assertions.assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_get_null_me_for_anonymous_user() {
    setAnonymousUser();

    Assertions.assertNull(meDatafetcher.getMe(AUTHORIZATION, mock(DataFetchingEnvironment.class)));
  }

  @Test
  public void should_get_null_me_when_principal_is_null() {
    setNullPrincipal();

    Assertions.assertNull(meDatafetcher.getMe(AUTHORIZATION, mock(DataFetchingEnvironment.class)));
  }

  @Test
  public void should_throw_not_found_when_user_is_missing() {
    setCurrentUser(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> meDatafetcher.getMe(AUTHORIZATION, dfe));
  }

  @Test
  public void should_get_user_payload_user_from_local_context() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<User>getLocalContext()).thenReturn(user);
    when(jwtService.toToken(eq(user))).thenReturn("new-token");

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getUserPayloadUser(dfe);

    Assertions.assertEquals(user.getEmail(), result.getData().getEmail());
    Assertions.assertEquals("new-token", result.getData().getToken());
  }
}
