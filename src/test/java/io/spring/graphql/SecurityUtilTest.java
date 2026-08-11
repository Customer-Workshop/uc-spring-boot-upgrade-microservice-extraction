package io.spring.graphql;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityUtilTest extends GraphQLTestBase {

  @Test
  public void should_return_current_user() {
    User user = new User("a@test.com", "a", "123", "", "");
    setCurrentUser(user);

    Optional<User> optional = SecurityUtil.getCurrentUser();

    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(user, optional.get());
  }

  @Test
  public void should_return_empty_for_anonymous_user() {
    setAnonymousUser();

    Assertions.assertFalse(SecurityUtil.getCurrentUser().isPresent());
  }

  @Test
  public void should_return_empty_when_principal_is_null() {
    setNullPrincipal();

    Assertions.assertFalse(SecurityUtil.getCurrentUser().isPresent());
  }
}
