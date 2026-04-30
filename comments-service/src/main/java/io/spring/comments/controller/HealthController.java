package io.spring.comments.controller;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/actuator/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Collections.singletonMap("status", "UP"));
  }
}
