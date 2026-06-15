package io.spring.api;

import io.spring.application.ProfileQueryService;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/internal")
@AllArgsConstructor
public class InternalProfileApi {
  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;

  @GetMapping(path = "profiles/{userId}")
  public ResponseEntity getProfileByUserId(@PathVariable("userId") String userId) {
    return userRepository
        .findById(userId)
        .map(
            user -> {
              Map<String, Object> profile = new HashMap<>();
              profile.put("id", user.getId());
              profile.put("username", user.getUsername());
              profile.put("bio", user.getBio());
              profile.put("image", user.getImage());
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("profile", profile);
                    }
                  });
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(path = "users/by-username/{username}")
  public ResponseEntity getUserIdByUsername(@PathVariable("username") String username) {
    return userRepository
        .findByUsername(username)
        .map(
            user -> {
              Map<String, Object> result = new HashMap<>();
              result.put("id", user.getId());
              result.put("username", user.getUsername());
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }
}
