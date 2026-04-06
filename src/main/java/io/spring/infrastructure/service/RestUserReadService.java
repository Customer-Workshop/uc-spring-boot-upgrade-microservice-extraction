package io.spring.infrastructure.service;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestUserReadService implements UserReadService {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public RestUserReadService(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public UserData findByUsername(String username) {
    try {
      return restTemplate.getForObject(
          userServiceUrl + "/api/internal/users/by-username/{username}",
          UserData.class,
          username);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }

  @Override
  public UserData findById(String id) {
    try {
      return restTemplate.getForObject(
          userServiceUrl + "/api/internal/users/{userId}", UserData.class, id);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }
}
