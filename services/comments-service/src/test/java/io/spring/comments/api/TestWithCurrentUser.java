package io.spring.comments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.comments.core.service.JwtService;
import io.spring.comments.core.user.CurrentUser;
import io.spring.comments.infrastructure.monolith.MonolithClient;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {
  @MockBean protected MonolithClient monolithClient;

  @MockBean protected JwtService jwtService;

  protected CurrentUser user;
  protected ProfileDTO profile;
  protected String token;
  protected String username;
  protected String defaultAvatar;

  protected void userFixture() {
    username = "johnjacob";
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new CurrentUser(UUID.randomUUID().toString(), username, "", defaultAvatar);
    profile = new ProfileDTO(user.getId(), username, "", defaultAvatar, false);
    when(monolithClient.findProfileById(eq(user.getId()), any())).thenReturn(profile);

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
