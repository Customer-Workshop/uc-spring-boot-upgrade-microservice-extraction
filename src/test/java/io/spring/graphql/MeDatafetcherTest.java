package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.User.Builder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest extends GraphQLTestBase {

  @Mock private UserQueryService userQueryService;

  @Mock private JwtService jwtService;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;
  private UserData userData;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), "bio", "image");
  }

  @Test
  public void should_return_current_user_with_token() {
    authenticate(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment);

    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals(user.getUsername(), result.getData().getUsername());
    assertEquals("jwt-token", result.getData().getToken());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_return_null_when_user_is_anonymous() {
    anonymous();

    assertNull(meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment));
  }

  @Test
  public void should_throw_not_found_when_current_user_is_missing_in_read_model() {
    authenticate(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment));
  }

  @Test
  public void should_build_user_payload_from_local_context() {
    when(dataFetchingEnvironment.<User>getLocalContext()).thenReturn(user);
    when(jwtService.toToken(eq(user))).thenReturn("new-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals("new-token", result.getData().getToken());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_expose_builder_for_graphql_user_type() {
    Builder builder = io.spring.graphql.types.User.newBuilder();

    assertEquals(
        "johnjacob", builder.username("johnjacob").email("e").token("t").build().getUsername());
  }
}
