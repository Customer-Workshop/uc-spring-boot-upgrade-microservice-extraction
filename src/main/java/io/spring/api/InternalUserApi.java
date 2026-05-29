package io.spring.api;

import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
@RequestMapping("/api/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserRepository userRepository;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{userId}/profile")
  public ResponseEntity<ProfileData> getProfileById(@PathVariable String userId) {
    Optional<User> user = userRepository.findById(userId);
    return user.map(u -> ResponseEntity.ok(toProfileData(u)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<ProfileData> getProfileByUsername(@PathVariable String username) {
    Optional<User> user = userRepository.findByUsername(username);
    return user.map(u -> ResponseEntity.ok(toProfileData(u)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/profiles")
  public ResponseEntity<List<ProfileData>> getProfilesByIds(@RequestBody List<String> userIds) {
    List<ProfileData> profiles =
        userIds.stream()
            .map(id -> userRepository.findById(id).map(this::toProfileData).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return ResponseEntity.ok(profiles);
  }

  @PostMapping("/following-authors")
  public ResponseEntity<Set<String>> getFollowingAuthors(
      @RequestBody FollowingAuthorsRequest request) {
    if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
      return ResponseEntity.ok(Collections.emptySet());
    }
    Set<String> following =
        userRelationshipQueryService.followingAuthors(request.getUserId(), request.getAuthorIds());
    return ResponseEntity.ok(following);
  }

  @GetMapping("/{userId}/followed")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }

  private ProfileData toProfileData(User user) {
    return new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
  }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
class FollowingAuthorsRequest {
  private String userId;
  private List<String> authorIds;
}
