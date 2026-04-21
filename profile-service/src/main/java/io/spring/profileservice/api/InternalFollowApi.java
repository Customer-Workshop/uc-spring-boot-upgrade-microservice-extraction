package io.spring.profileservice.api;

import io.spring.profileservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/internal/follows")
@AllArgsConstructor
public class InternalFollowApi {

  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping(path = "/is-following")
  public ResponseEntity<Boolean> isFollowing(
      @RequestParam("userId") String userId, @RequestParam("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(following);
  }

  @PostMapping(path = "/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @RequestBody FollowingAuthorsRequest request) {
    Set<String> followedAuthorIds =
        userRelationshipQueryService.followingAuthors(request.getUserId(), request.getAuthorIds());
    return ResponseEntity.ok(followedAuthorIds);
  }

  @GetMapping(path = "/followed-users/{userId}")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    List<String> followedUserIds = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUserIds);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FollowingAuthorsRequest {
    private String userId;
    private List<String> authorIds;
  }
}
