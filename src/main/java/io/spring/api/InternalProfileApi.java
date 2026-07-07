package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal service-to-service endpoint used by the comments microservice to resolve author profiles
 * (users/profiles bounded context) including the follow relationship relative to a viewer. Not part
 * of the public API.
 */
@RestController
@RequestMapping(path = "/internal/profiles")
@AllArgsConstructor
public class InternalProfileApi {
  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping
  public ResponseEntity<InternalProfileResponse> getProfile(
      @RequestParam("userId") String userId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    boolean following =
        viewerId != null && userRelationshipQueryService.isUserFollowing(viewerId, userId);
    return ResponseEntity.ok(
        new InternalProfileResponse(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            following));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InternalProfileResponse {
    private String id;
    private String username;
    private String bio;
    private String image;
    private boolean following;
  }
}
