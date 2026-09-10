package io.spring.favorites.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.favorites.core.service.JwtService;
import io.spring.favorites.core.user.CurrentUser;
import io.spring.favorites.infrastructure.monolith.MonolithClient;
import io.spring.favorites.infrastructure.monolith.dto.ProfileDto;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class TestWithCurrentUser {
  @MockBean protected MonolithClient monolithClient;

  @MockBean protected JwtService jwtService;

  protected CurrentUser user;
  protected ProfileDto profile;
  protected String token;
  protected String username;
  protected String defaultAvatar;

  protected void userFixture() {
    username = "johnjacob";
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new CurrentUser(UUID.randomUUID().toString(), username, "", defaultAvatar);
    profile = new ProfileDto(user.getId(), username, "", defaultAvatar, false);
    when(monolithClient.findProfileById(eq(user.getId()), eq(user.getId()))).thenReturn(profile);

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @BeforeEach
  public void setUp() throws Exception {
    userFixture();
  }
}
