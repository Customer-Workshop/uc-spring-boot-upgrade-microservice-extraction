package io.spring.api;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
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
@RequestMapping(path = "/internal")
@AllArgsConstructor
public class InternalUserApi {
  private UserRepository userRepository;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping(path = "/users/{userId}")
  public ResponseEntity getUserById(@PathVariable("userId") String userId) {
    return userRepository
        .findById(userId)
        .map(this::userResponse)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(path = "/users/{userId}/following")
  public ResponseEntity getFollowedUsers(@PathVariable("userId") String userId) {
    List<String> followedUserIds = userRelationshipQueryService.followedUsers(userId);
    Map<String, Object> response = new HashMap<>();
    response.put("followedUserIds", followedUserIds);
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "/profiles/{username}")
  public ResponseEntity getProfileByUsername(@PathVariable("username") String username) {
    return userRepository
        .findByUsername(username)
        .map(this::profileWithIdResponse)
        .orElse(ResponseEntity.notFound().build());
  }

  private ResponseEntity userResponse(User user) {
    Map<String, Object> userData = new HashMap<>();
    userData.put("id", user.getId());
    userData.put("username", user.getUsername());
    userData.put("bio", user.getBio());
    userData.put("image", user.getImage());
    Map<String, Object> response = new HashMap<>();
    response.put("user", userData);
    return ResponseEntity.ok(response);
  }

  private ResponseEntity profileWithIdResponse(User user) {
    Map<String, Object> profileData = new HashMap<>();
    profileData.put("id", user.getId());
    profileData.put("username", user.getUsername());
    profileData.put("bio", user.getBio());
    profileData.put("image", user.getImage());
    Map<String, Object> response = new HashMap<>();
    response.put("profile", profileData);
    return ResponseEntity.ok(response);
  }
}
