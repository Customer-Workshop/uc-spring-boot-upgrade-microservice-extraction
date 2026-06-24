package io.spring.api;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUsersApi {

  private final UserRepository userRepository;
  private final UserRelationshipQueryService userRelationshipQueryService;

  public InternalUsersApi(
      UserRepository userRepository, UserRelationshipQueryService userRelationshipQueryService) {
    this.userRepository = userRepository;
    this.userRelationshipQueryService = userRelationshipQueryService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
    Optional<User> user = userRepository.findById(id);
    return user.map(u -> ResponseEntity.ok(toMap(u))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
    Optional<User> user = userRepository.findByUsername(username);
    return user.map(u -> ResponseEntity.ok(toMap(u))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/following/{targetId}")
  public ResponseEntity<Map<String, Object>> isFollowing(
      @PathVariable String id, @PathVariable String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(id, targetId);
    Map<String, Object> result = new HashMap<>();
    result.put("following", following);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/following")
  public ResponseEntity<Map<String, Object>> getFollowedUsers(@PathVariable String id) {
    List<String> userIds = userRelationshipQueryService.followedUsers(id);
    Map<String, Object> result = new HashMap<>();
    result.put("userIds", userIds);
    return ResponseEntity.ok(result);
  }

  private Map<String, Object> toMap(User user) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", user.getId());
    map.put("email", user.getEmail());
    map.put("username", user.getUsername());
    map.put("bio", user.getBio());
    map.put("image", user.getImage());
    return map;
  }
}
