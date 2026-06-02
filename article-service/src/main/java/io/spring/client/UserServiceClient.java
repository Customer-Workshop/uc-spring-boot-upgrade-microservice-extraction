package io.spring.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserDto> findById(String id) {
    try {
      UserDto user =
          restTemplate.getForObject(userServiceUrl + "/users/internal/" + id, UserDto.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
