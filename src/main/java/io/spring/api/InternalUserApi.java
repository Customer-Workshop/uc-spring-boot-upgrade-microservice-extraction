package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/users")
@AllArgsConstructor
public class InternalUserApi {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping(path = "/{userId}/profile")
  public ResponseEntity<?> getProfileByUserId(@PathVariable("userId") String userId) {
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

  @GetMapping(path = "/{userId}/following")
  public ResponseEntity<?> getFollowedUsers(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers != null ? followedUsers : Collections.emptyList());
  }

  @GetMapping(path = "/{userId}/following/{targetId}")
  public ResponseEntity<?> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(following);
  }
}
