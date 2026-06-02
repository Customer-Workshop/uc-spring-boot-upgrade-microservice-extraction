package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping(path = "/{id}/profile")
  public ResponseEntity<?> getUserProfile(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
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

  @GetMapping(path = "/{userId}/following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    return ResponseEntity.ok(userRelationshipQueryService.isUserFollowing(userId, targetId));
  }

  @PostMapping(path = "/following-authors")
  public ResponseEntity<?> followingAuthors(@RequestBody FollowingAuthorsRequest request) {
    Set<String> following =
        userRelationshipQueryService.followingAuthors(request.getUserId(), request.getAuthorIds());
    return ResponseEntity.ok(following);
  }

  @GetMapping(path = "/{userId}/followed")
  public ResponseEntity<?> followedUsers(@PathVariable("userId") String userId) {
    List<String> followed = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followed);
  }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
class FollowingAuthorsRequest {
  private String userId;
  private List<String> authorIds;
}
