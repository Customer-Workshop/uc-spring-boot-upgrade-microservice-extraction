package io.spring.profileservice.client;

import io.spring.profileservice.application.data.UserData;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClientImpl implements UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceBaseUrl;

  public UserServiceClientImpl(
      RestTemplate restTemplate,
      @Value("${user-service.base-url}") String userServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.userServiceBaseUrl = userServiceBaseUrl;
  }

  @Override
  public Optional<UserData> findByUsername(String username) {
    try {
      UserData userData =
          restTemplate.getForObject(
              userServiceBaseUrl + "/api/internal/users/by-username/{username}",
              UserData.class,
              username);
      return Optional.ofNullable(userData);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserData> findById(String id) {
    try {
      UserData userData =
          restTemplate.getForObject(
              userServiceBaseUrl + "/api/internal/users/{id}", UserData.class, id);
      return Optional.ofNullable(userData);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
