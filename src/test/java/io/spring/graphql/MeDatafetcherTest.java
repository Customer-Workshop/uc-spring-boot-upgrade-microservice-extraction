package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Collections;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_current_user() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    UserData userData =
        new UserData(
            user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    var result = meDatafetcher.getMe("Token mytoken", null);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    var result = meDatafetcher.getMe("Token token", null);

    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(null, null));

    var result = meDatafetcher.getMe("Token token", null);

    assertNull(result);
  }

  @Test
  void should_throw_not_found_when_user_not_in_db() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token token", null));
  }

  @Test
  void should_get_user_payload_user() {
    when(jwtService.toToken(eq(user))).thenReturn("generated-token");

    org.mockito.MockedStatic<SecurityContextHolder> mockedStatic = null;
    // Use a mock DataFetchingEnvironment to test getUserPayloadUser
    graphql.schema.DataFetchingEnvironment dfe = mock(graphql.schema.DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    var result = meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("generated-token", result.getData().getToken());
    assertEquals(user, result.getLocalContext());
  }
}
