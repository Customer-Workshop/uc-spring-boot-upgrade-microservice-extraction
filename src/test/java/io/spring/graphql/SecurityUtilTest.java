package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtilTest extends GraphQLTestBase {

  @Test
  public void should_return_current_user_when_authenticated() {
    User user = new User("john@jacob.com", "johnjacob", "123", "", "");
    authenticate(user);

    Optional<User> current = SecurityUtil.getCurrentUser();

    assertTrue(current.isPresent());
    assertEquals(user.getId(), current.get().getId());
  }

  @Test
  public void should_return_empty_when_anonymous() {
    anonymous();

    assertFalse(SecurityUtil.getCurrentUser().isPresent());
  }

  @Test
  public void should_return_empty_when_principal_is_null() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    assertFalse(SecurityUtil.getCurrentUser().isPresent());
  }
}
