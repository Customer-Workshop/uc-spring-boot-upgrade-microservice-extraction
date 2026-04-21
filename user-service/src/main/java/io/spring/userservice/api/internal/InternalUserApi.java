package io.spring.userservice.api.internal;

import io.spring.userservice.application.UserQueryService;
import io.spring.userservice.application.data.UserData;
import io.spring.userservice.core.service.JwtService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/internal")
@AllArgsConstructor
public class InternalUserApi {

  private UserQueryService userQueryService;
  private JwtService jwtService;

  @GetMapping("/users/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    Optional<UserData> userData = userQueryService.findById(id);
    return userData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/auth/validate")
  public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    if (token == null || token.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    Optional<String> userId = jwtService.getSubFromToken(token);
    if (userId.isPresent()) {
      Optional<UserData> userData = userQueryService.findById(userId.get());
      if (userData.isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("userId", userId.get());
        response.put("user", userData.get());
        return ResponseEntity.ok(response);
      }
    }

    Map<String, Object> response = new HashMap<>();
    response.put("valid", false);
    return ResponseEntity.ok(response);
  }
}
