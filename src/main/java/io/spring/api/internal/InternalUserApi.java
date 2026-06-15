package io.spring.api.internal;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@RequestMapping(path = "/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{id}/profile")
  public ResponseEntity<?> getProfile(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> result = new HashMap<>();
    result.put("id", userData.getId());
    result.put("username", userData.getUsername());
    result.put("bio", userData.getBio());
    result.put("image", userData.getImage());
    return ResponseEntity.ok(result);
  }

  @PostMapping("/profiles")
  public ResponseEntity<?> getProfiles(@RequestBody List<String> userIds) {
    List<Map<String, Object>> profiles = new ArrayList<>();
    for (String userId : userIds) {
      UserData userData = userReadService.findById(userId);
      if (userData != null) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userData.getId());
        profile.put("username", userData.getUsername());
        profile.put("bio", userData.getBio());
        profile.put("image", userData.getImage());
        profiles.add(profile);
      }
    }
    return ResponseEntity.ok(profiles);
  }

  @GetMapping("/{userId}/following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @PostMapping("/{userId}/following-authors")
  public ResponseEntity<?> getFollowingAuthors(
      @PathVariable("userId") String userId, @RequestBody List<String> authorIds) {
    Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(followingAuthors);
  }

  @GetMapping("/{userId}/followed")
  public ResponseEntity<?> getFollowedUserIds(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }
}
