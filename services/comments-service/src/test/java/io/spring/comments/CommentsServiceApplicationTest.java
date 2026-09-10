package io.spring.comments;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class CommentsServiceApplicationTest {

  @Test
  public void context_loads() {}
}
