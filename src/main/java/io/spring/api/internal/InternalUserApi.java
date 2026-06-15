package io.spring.api.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/users")
public class InternalUserApi {

  private static final ObjectMapper PLAIN_MAPPER = new ObjectMapper();

  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  public InternalUserApi(
      UserReadService userReadService, UserRelationshipQueryService userRelationshipQueryService) {
    this.userReadService = userReadService;
    this.userRelationshipQueryService = userRelationshipQueryService;
  }

  @GetMapping("/{id}/profile")
  public ResponseEntity<?> getProfile(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toProfileMap(userData));
  }

  @PostMapping("/profiles")
  public ResponseEntity<?> getProfiles(@RequestBody String body) {
    List<String> userIds;
    try {
      userIds = PLAIN_MAPPER.readValue(body, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
    List<Map<String, Object>> profiles = new ArrayList<>();
    for (String userId : userIds) {
      UserData userData = userReadService.findById(userId);
      if (userData != null) {
        profiles.add(toProfileMap(userData));
      }
    }
    return ResponseEntity.ok(profiles);
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<?> getByUsername(@PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toProfileMap(userData));
  }

  @GetMapping("/{userId}/following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @PostMapping("/{userId}/following-authors")
  public ResponseEntity<?> getFollowingAuthors(
      @PathVariable("userId") String userId, @RequestBody String body) {
    List<String> authorIds;
    try {
      authorIds = PLAIN_MAPPER.readValue(body, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
    Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(followingAuthors);
  }

  @GetMapping("/{userId}/followed")
  public ResponseEntity<?> getFollowedUserIds(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }

  private Map<String, Object> toProfileMap(UserData userData) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", userData.getId());
    result.put("username", userData.getUsername());
    result.put("bio", userData.getBio());
    result.put("image", userData.getImage());
    return result;
  }
}
