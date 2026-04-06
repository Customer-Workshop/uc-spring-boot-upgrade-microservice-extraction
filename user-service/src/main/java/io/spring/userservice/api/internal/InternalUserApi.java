package io.spring.userservice.api.internal;

import io.spring.userservice.application.data.ProfileData;
import io.spring.userservice.application.data.UserData;
import io.spring.userservice.core.user.User;
import io.spring.userservice.core.user.UserRepository;
import io.spring.userservice.infrastructure.mybatis.readservice.UserReadService;
import io.spring.userservice.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@AllArgsConstructor
public class InternalUserApi {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private UserRepository userRepository;

  @GetMapping("/users/{userId}")
  public ResponseEntity<UserData> getUserById(@PathVariable("userId") String userId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-username/{username}")
  public ResponseEntity<UserData> getUserByUsername(
      @PathVariable("username") String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    UserData userData = userReadService.findById(userOpt.get().getId());
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-email/{email}")
  public ResponseEntity<UserData> getUserByEmail(@PathVariable("email") String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    UserData userData = userReadService.findById(userOpt.get().getId());
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/{userId}/following/{targetUserId}")
  public ResponseEntity<Boolean> isUserFollowing(
      @PathVariable("userId") String userId,
      @PathVariable("targetUserId") String targetUserId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetUserId);
    return ResponseEntity.ok(following);
  }

  @GetMapping("/profiles/batch")
  public ResponseEntity<List<UserData>> batchGetProfiles(
      @RequestParam("userIds") List<String> userIds) {
    List<UserData> profiles = new ArrayList<>();
    for (String userId : userIds) {
      UserData userData = userReadService.findById(userId);
      if (userData != null) {
        profiles.add(userData);
      }
    }
    return ResponseEntity.ok(profiles);
  }

  @GetMapping("/users/{userId}/following-authors")
  public ResponseEntity<Set<String>> followingAuthors(
      @PathVariable("userId") String userId,
      @RequestParam("ids") List<String> ids) {
    Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(userId, ids);
    return ResponseEntity.ok(followingAuthors);
  }

  @GetMapping("/users/{userId}/followed-users")
  public ResponseEntity<List<String>> followedUsers(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }
}
