package io.spring.userservice.api;

import io.spring.userservice.api.exception.ResourceNotFoundException;
import io.spring.userservice.domain.FollowRelation;
import io.spring.userservice.domain.ProfileData;
import io.spring.userservice.domain.User;
import io.spring.userservice.domain.UserData;
import io.spring.userservice.repository.UserMapper;
import io.spring.userservice.repository.UserReadService;
import io.spring.userservice.repository.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private UserMapper userMapper;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping
  public ResponseEntity<Object> getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return findProfile(username, user)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity<Object> follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return userMapper
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              userMapper.saveRelation(followRelation);
              return profileResponse(findProfile(username, user).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity<Object> unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Optional<User> userOptional = userMapper.findByUsername(username);
    if (userOptional.isPresent()) {
      User target = userOptional.get();
      return userMapper
          .findRelation(user.getId(), target.getId())
          .map(
              relation -> {
                userMapper.deleteRelation(relation);
                return profileResponse(findProfile(username, user).get());
              })
          .orElseThrow(ResourceNotFoundException::new);
    } else {
      throw new ResourceNotFoundException();
    }
  }

  private Optional<ProfileData> findProfile(String username, User currentUser) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    }
    ProfileData profileData =
        new ProfileData(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            currentUser != null
                && userRelationshipQueryService.isUserFollowing(
                    currentUser.getId(), userData.getId()));
    return Optional.of(profileData);
  }

  private ResponseEntity<Object> profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
