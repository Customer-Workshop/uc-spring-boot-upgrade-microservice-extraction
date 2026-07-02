package io.spring.graphql;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utility for extracting the authenticated {@link User} from the Spring Security context. */
public class SecurityUtil {

  /**
   * Returns the currently authenticated user from the security context.
   *
   * @return the authenticated user, or empty if the request is anonymous
   */
  public static Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return Optional.empty();
    }
    io.spring.core.user.User currentUser = (io.spring.core.user.User) authentication.getPrincipal();
    return Optional.of(currentUser);
  }
}
