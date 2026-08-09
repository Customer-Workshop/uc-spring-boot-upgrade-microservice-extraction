package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest extends GraphqlTestBase {
  @Test
  void returnsEmptyForAnonymousAuthentication() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertTrue(SecurityUtil.getCurrentUser().isEmpty());
  }

  @Test
  void returnsAuthenticatedUser() {
    var user = GraphqlTestFixtures.user();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    assertEquals(user, SecurityUtil.getCurrentUser().orElseThrow());
  }
}
