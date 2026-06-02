package io.spring.api;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/internal/users")
@AllArgsConstructor
public class InternalUserApi {
  private UserRelationshipQueryService userRelationshipQueryService;
  private UserReadService userReadService;
  private UserRepository userRepository;

  @GetMapping(path = "/{userId}/following/{targetId}")
  public ResponseEntity<Boolean> isUserFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(following);
  }

  @PostMapping(path = "/{userId}/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @PathVariable("userId") String userId, @RequestBody List<String> authorIds) {
    Set<String> result = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(result);
  }

  @GetMapping(path = "/{userId}/followed-users")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    List<String> result = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(result);
  }

  @GetMapping(path = "/{userId}")
  public ResponseEntity<?> getUserById(@PathVariable("userId") String userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isPresent()) {
      User u = user.get();
      Map<String, Object> result = new HashMap<>();
      result.put("id", u.getId());
      result.put("email", u.getEmail());
      result.put("username", u.getUsername());
      result.put("bio", u.getBio());
      result.put("image", u.getImage());
      return ResponseEntity.ok(result);
    }
    return ResponseEntity.notFound().build();
  }
}
