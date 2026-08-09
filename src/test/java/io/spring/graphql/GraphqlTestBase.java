package io.spring.graphql;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;

abstract class GraphqlTestBase {
  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }
}
