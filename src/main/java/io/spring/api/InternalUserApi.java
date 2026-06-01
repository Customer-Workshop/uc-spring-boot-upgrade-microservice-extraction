package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/internal/users")
@AllArgsConstructor
public class InternalUserApi {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{userId}/profile")
  public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String userId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> profile = new HashMap<>();
    profile.put("id", userData.getId());
    profile.put("username", userData.getUsername());
    profile.put("bio", userData.getBio());
    profile.put("image", userData.getImage());
    return ResponseEntity.ok(profile);
  }

  @GetMapping("/profiles")
  public ResponseEntity<Map<String, Map<String, Object>>> getUserProfiles(
      @RequestParam("ids") List<String> ids) {
    Map<String, Map<String, Object>> result = new HashMap<>();
    for (String id : ids) {
      UserData userData = userReadService.findById(id);
      if (userData != null) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userData.getId());
        profile.put("username", userData.getUsername());
        profile.put("bio", userData.getBio());
        profile.put("image", userData.getImage());
        result.put(id, profile);
      }
    }
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{userId}/following/{targetId}")
  public ResponseEntity<Map<String, Boolean>> isUserFollowing(
      @PathVariable String userId, @PathVariable String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(Collections.singletonMap("following", following));
  }

  @GetMapping("/{userId}/following")
  public ResponseEntity<Map<String, Object>> followingAuthors(
      @PathVariable String userId, @RequestParam("ids") List<String> ids) {
    Set<String> followingIds = userRelationshipQueryService.followingAuthors(userId, ids);
    Map<String, Object> result = new HashMap<>();
    result.put("following", !followingIds.isEmpty());
    result.put("followingIds", followingIds);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{userId}/followed")
  public ResponseEntity<List<String>> followedUsers(@PathVariable String userId) {
    List<String> followed = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followed);
  }
}
