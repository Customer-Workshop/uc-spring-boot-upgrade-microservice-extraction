package io.spring.articleservice.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthApi {

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP", "service", "article-service");
  }
}
