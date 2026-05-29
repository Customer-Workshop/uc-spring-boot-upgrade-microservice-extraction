package io.spring.api;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/users")
@AllArgsConstructor
public class InternalUserApi {

  private UserRepository userRepository;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/{userId}/profile")
  public ResponseEntity<InternalProfileData> getProfileById(@PathVariable String userId) {
    Optional<User> user = userRepository.findById(userId);
    return user.map(u -> ResponseEntity.ok(toInternalProfile(u)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<InternalProfileData> getProfileByUsername(@PathVariable String username) {
    Optional<User> user = userRepository.findByUsername(username);
    return user.map(u -> ResponseEntity.ok(toInternalProfile(u)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/profiles")
  public ResponseEntity<List<InternalProfileData>> getProfilesByIds(
      @RequestParam List<String> ids) {
    List<InternalProfileData> profiles =
        ids.stream()
            .map(id -> userRepository.findById(id).map(this::toInternalProfile).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return ResponseEntity.ok(profiles);
  }

  @GetMapping("/following-authors")
  public ResponseEntity<Set<String>> getFollowingAuthors(
      @RequestParam String userId, @RequestParam List<String> authorIds) {
    if (authorIds.isEmpty()) {
      return ResponseEntity.ok(Collections.emptySet());
    }
    Set<String> following = userRelationshipQueryService.followingAuthors(userId, authorIds);
    return ResponseEntity.ok(following);
  }

  @GetMapping("/{userId}/followed")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }

  private InternalProfileData toInternalProfile(User user) {
    return new InternalProfileData(
        user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
  }
}
