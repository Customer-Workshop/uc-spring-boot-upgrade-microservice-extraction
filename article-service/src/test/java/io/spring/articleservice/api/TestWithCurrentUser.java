package io.spring.articleservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.articleservice.core.service.JwtService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {

  protected String userId;
  protected String username;
  protected String token;
  protected String defaultAvatar;

  @MockBean protected JwtService jwtService;

  protected void userFixture() {
    userId = UUID.randomUUID().toString();
    username = "johnjacob";
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(userId));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
