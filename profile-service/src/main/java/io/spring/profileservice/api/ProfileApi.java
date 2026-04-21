package io.spring.profileservice.api;

import io.spring.profileservice.api.exception.ResourceNotFoundException;
import io.spring.profileservice.application.ProfileQueryService;
import io.spring.profileservice.application.data.ProfileData;
import io.spring.profileservice.application.data.UserData;
import io.spring.profileservice.client.UserServiceClient;
import io.spring.profileservice.core.FollowRelation;
import io.spring.profileservice.core.FollowRelationRepository;
import java.util.HashMap;
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
  private ProfileQueryService profileQueryService;
  private UserServiceClient userServiceClient;
  private FollowRelationRepository followRelationRepository;

  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username,
      @AuthenticationPrincipal String currentUserId) {
    return profileQueryService
        .findByUsername(username, currentUserId)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username,
      @AuthenticationPrincipal String currentUserId) {
    return userServiceClient
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(currentUserId, target.getId());
              followRelationRepository.saveRelation(followRelation);
              return profileResponse(
                  profileQueryService.findByUsername(username, currentUserId).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username,
      @AuthenticationPrincipal String currentUserId) {
    UserData target =
        userServiceClient.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    return followRelationRepository
        .findRelation(currentUserId, target.getId())
        .map(
            relation -> {
              followRelationRepository.removeRelation(relation);
              return profileResponse(
                  profileQueryService.findByUsername(username, currentUserId).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
