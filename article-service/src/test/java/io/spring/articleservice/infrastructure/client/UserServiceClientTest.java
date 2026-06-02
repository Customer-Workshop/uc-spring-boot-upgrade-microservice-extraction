package io.spring.articleservice.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

  @Mock private RestTemplate restTemplate;

  private UserServiceClient userServiceClient;

  @BeforeEach
  void setUp() {
    userServiceClient =
        new UserServiceClient(restTemplate, "http://localhost:8080", "test-api-key");
  }

  @Test
  void getProfile_success() {
    UserProfileResponse response = new UserProfileResponse("user-1", "johndoe", "bio", "image");
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(),
            eq(UserProfileResponse.class),
            eq("user-1")))
        .thenReturn(ResponseEntity.ok(response));

    ProfileData profile = userServiceClient.getProfile("user-1");
    assertEquals("johndoe", profile.getUsername());
    assertEquals("bio", profile.getBio());
  }

  @Test
  void getProfile_fallback_on_error() {
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(),
            eq(UserProfileResponse.class),
            eq("user-1")))
        .thenThrow(new RestClientException("Connection refused"));

    ProfileData profile = userServiceClient.getProfile("user-1");
    assertNotNull(profile);
    assertEquals("unknown", profile.getUsername());
    assertFalse(profile.isFollowing());
  }

  @Test
  void isUserFollowing_fallback_on_error() {
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(),
            eq(Boolean.class),
            eq("user-1"),
            eq("user-2")))
        .thenThrow(new RestClientException("Connection refused"));

    boolean result = userServiceClient.isUserFollowing("user-1", "user-2");
    assertFalse(result);
  }

  @Test
  void followingAuthors_fallback_on_error() {
    when(restTemplate.exchange(
            any(String.class), any(), any(), any(ParameterizedTypeReference.class)))
        .thenThrow(new RestClientException("Connection refused"));

    Set<String> result =
        userServiceClient.followingAuthors("user-1", Arrays.asList("user-2", "user-3"));
    assertNotNull(result);
    assertEquals(0, result.size());
  }
}
