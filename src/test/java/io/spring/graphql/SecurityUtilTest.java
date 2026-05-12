package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtilTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals(user, result.get());
  }

  @Test
  void should_return_empty_when_anonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }

  @Test
  void should_return_empty_when_null_principal() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }
}
