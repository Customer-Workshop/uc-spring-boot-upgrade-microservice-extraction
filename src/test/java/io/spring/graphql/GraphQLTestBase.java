package io.spring.graphql;

import io.spring.core.user.User;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared security context helpers for the GraphQL datafetcher and mutation unit tests. */
abstract class GraphQLTestBase {

  protected void authenticate(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
  }

  protected void anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }
}
