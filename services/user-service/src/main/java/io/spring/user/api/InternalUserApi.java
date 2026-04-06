package io.spring.user.api;

import io.spring.shared.dto.ProfileData;
import io.spring.shared.dto.UserData;
import io.spring.user.infrastructure.mybatis.readservice.UserReadService;
import io.spring.user.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.List;
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

  @GetMapping("/{id}")
  public ResponseEntity<ProfileData> getUserProfile(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
    return ResponseEntity.ok(profileData);
  }

  @PostMapping("/batch")
  public ResponseEntity<List<ProfileData>> batchGetProfiles(@RequestBody List<String> userIds) {
    List<ProfileData> profiles = new ArrayList<>();
    for (String userId : userIds) {
      UserData userData = userReadService.findById(userId);
      if (userData != null) {
        profiles.add(
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false));
      }
    }
    return ResponseEntity.ok(profiles);
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<ProfileData> getUserByUsername(
      @PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            false);
    return ResponseEntity.ok(profileData);
  }

  @GetMapping("/{id}/following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("id") String userId, @PathVariable("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(following);
  }

  @PostMapping("/{id}/following-authors")
  public ResponseEntity<List<String>> followingAuthors(
      @PathVariable("id") String userId, @RequestBody List<String> authorIds) {
    java.util.Set<String> result = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(new ArrayList<>(result));
  }

  @GetMapping("/{id}/followed-users")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("id") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }
}
