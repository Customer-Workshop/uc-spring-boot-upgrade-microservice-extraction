package io.spring.api.internal;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoint consumed by the comments microservice to resolve author profiles. Not
 * part of the public RealWorld API.
 */
@RestController
@RequestMapping(path = "/internal/profiles")
@AllArgsConstructor
public class InternalProfileApi {
  private ProfileQueryService profileQueryService;

  @GetMapping("/{userId}")
  public ResponseEntity<InternalProfileResponse> getProfile(
      @PathVariable("userId") String userId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    ProfileData profile =
        profileQueryService
            .findByUserId(userId, viewerId)
            .orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(
        new InternalProfileResponse(
            profile.getId(),
            profile.getUsername(),
            profile.getBio(),
            profile.getImage(),
            profile.isFollowing()));
  }
}
