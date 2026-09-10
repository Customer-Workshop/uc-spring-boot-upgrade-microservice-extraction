package io.spring.api;

import io.spring.application.SystemReadinessQueryService;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "system")
@AllArgsConstructor
public class SystemReadinessApi {
  private SystemReadinessQueryService systemReadinessQueryService;

  @GetMapping("readiness")
  public ResponseEntity getReadiness() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("readiness", systemReadinessQueryService.readiness());
          }
        });
  }
}
