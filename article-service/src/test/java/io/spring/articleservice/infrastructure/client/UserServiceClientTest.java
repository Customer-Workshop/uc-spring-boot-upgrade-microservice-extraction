package io.spring.articleservice.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.articleservice.application.data.ProfileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class UserServiceClientTest {

  @Mock private RestTemplate restTemplate;

  private UserServiceClient userServiceClient;

  @BeforeEach
  void setUp() {
    userServiceClient =
        new UserServiceClient(restTemplate, "http://localhost:8080", "test-api-key");
  }

  @Test
  void should_return_profile_on_success() {
    UserServiceClient.ProfileResponse.Profile profile =
        new UserServiceClient.ProfileResponse.Profile();
    profile.setUsername("testuser");
    profile.setBio("bio");
    profile.setImage("image.jpg");

    UserServiceClient.ProfileResponse response = new UserServiceClient.ProfileResponse();
    response.setProfile(profile);

    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserServiceClient.ProfileResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    ProfileData result = userServiceClient.getProfile("user-123");

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image.jpg", result.getImage());
    assertEquals("user-123", result.getId());
  }

  @Test
  void should_return_default_profile_on_failure() {
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserServiceClient.ProfileResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    ProfileData result = userServiceClient.getProfile("user-123");

    assertNotNull(result);
    assertEquals("user-123", result.getUsername());
    assertEquals("user-123", result.getId());
  }

  @Test
  void should_resolve_username_to_user_id() {
    UserServiceClient.UserLookupResponse.UserData userData =
        new UserServiceClient.UserLookupResponse.UserData();
    userData.setId("resolved-user-id");
    userData.setUsername("testuser");
    UserServiceClient.UserLookupResponse lookupResponse =
        new UserServiceClient.UserLookupResponse();
    lookupResponse.setUser(userData);

    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserServiceClient.UserLookupResponse.class)))
        .thenReturn(ResponseEntity.ok(lookupResponse));

    String result = userServiceClient.resolveUsernameToUserId("testuser");
    assertEquals("resolved-user-id", result);
  }

  @Test
  void should_return_null_on_username_resolve_failure() {
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserServiceClient.UserLookupResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    String result = userServiceClient.resolveUsernameToUserId("testuser");
    assertNull(result);
  }
}
