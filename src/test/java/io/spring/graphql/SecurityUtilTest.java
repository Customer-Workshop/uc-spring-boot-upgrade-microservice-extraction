package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtilTest {

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_throw_when_no_authentication() {
    SecurityContextHolder.clearContext();
    assertThrows(NullPointerException.class, () -> SecurityUtil.getCurrentUser());
  }

  @Test
  public void should_return_empty_when_anonymous_authentication() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertFalse(result.isPresent());
  }

  @Test
  public void should_return_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "123", "bio", "image");
    TestingAuthenticationToken authToken = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(authToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  public void should_return_empty_when_principal_is_null() {
    TestingAuthenticationToken authToken = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(authToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertFalse(result.isPresent());
  }
}
