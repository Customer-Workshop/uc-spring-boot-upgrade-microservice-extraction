package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/users")
@AllArgsConstructor
public class InternalUsersApi {
  private UserRepository userRepository;
  private UserReadService userReadService;

  @GetMapping("/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<UserData> getUserByUsername(@PathVariable("username") String username) {
    Optional<User> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    UserData userData = userReadService.findById(user.get().getId());
    return ResponseEntity.ok(userData);
  }
}
