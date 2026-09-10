package io.spring.comments.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.comments.api.exception.ResourceNotFoundException;
import io.spring.comments.core.service.JwtService;
import io.spring.comments.core.user.CurrentUser;
import io.spring.comments.infrastructure.monolith.MonolithClient;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Exercises the real servlet ERROR dispatch, which MockMvc skips: Spring Security 6 runs the filter
 * chain on that dispatch too, so a chain that does not permit it turns every {@code sendError}
 * status into an empty 401.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErrorDispatchTest {

  @MockBean private MonolithClient monolithClient;
  @MockBean private JwtService jwtService;

  @Autowired private TestRestTemplate restTemplate;

  private final String token = "token";
  private CurrentUser user;

  @BeforeEach
  public void setUp() {
    user = new CurrentUser(UUID.randomUUID().toString(), "johnjacob", "", "");
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
    when(monolithClient.findProfileById(eq(user.getId()), any()))
        .thenReturn(new ProfileDTO(user.getId(), user.getUsername(), "", "", false));
  }

  @Test
  public void should_keep_404_status_through_error_dispatch() {
    when(monolithClient.findArticleBySlug(eq("unknown")))
        .thenThrow(new ResourceNotFoundException());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/articles/unknown/comments", HttpMethod.GET, authenticated(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("\"status\":404");
  }

  private HttpEntity<Void> authenticated() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Token " + token);
    return new HttpEntity<>(headers);
  }
}
