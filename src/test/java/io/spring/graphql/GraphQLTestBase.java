package io.spring.graphql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

abstract class GraphQLTestBase {

  protected static final String AUTHORIZATION = "Token jwt-token";

  /**
   * The security filter chain always populates an authentication, so tests default to an anonymous
   * one instead of an empty security context.
   */
  @BeforeEach
  void setAnonymousUserByDefault() {
    setAnonymousUser();
  }

  protected void setCurrentUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES));
  }

  protected void setAnonymousUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  protected void setNullPrincipal() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }
}
