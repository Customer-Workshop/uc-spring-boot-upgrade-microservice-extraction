package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
  @Mock private DataFetchingEnvironment dfe;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_me_success() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UserData userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    var result = meDatafetcher.getMe("Token mytoken", dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  public void should_return_null_when_anonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    var result = meDatafetcher.getMe("Token mytoken", dfe);

    assertNull(result);
  }

  @Test
  public void should_return_null_when_principal_is_null() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    var result = meDatafetcher.getMe("Token mytoken", dfe);

    assertNull(result);
  }

  @Test
  public void should_throw_not_found_when_user_data_not_found() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token mytoken", dfe));
  }

  @Test
  public void should_get_user_payload_user() {
    when(dfe.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(eq(user))).thenReturn("generatedToken");

    var result = meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("generatedToken", result.getData().getToken());
  }
}
