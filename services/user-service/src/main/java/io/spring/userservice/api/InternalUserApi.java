package io.spring.userservice.api;

import io.spring.userservice.core.user.User;
import io.spring.userservice.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserRepository userRepository;

  @GetMapping("/{userId}")
  public ResponseEntity<?> getUserById(@PathVariable String userId) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    User user = userOpt.get();
    Map<String, Object> body = new HashMap<>();
    body.put("id", user.getId());
    body.put("email", user.getEmail());
    body.put("username", user.getUsername());
    body.put("bio", user.getBio());
    body.put("image", user.getImage());
    return ResponseEntity.ok(body);
  }
}
