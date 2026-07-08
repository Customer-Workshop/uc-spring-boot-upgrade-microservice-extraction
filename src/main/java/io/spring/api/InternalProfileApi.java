package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal (service-to-service) endpoint that lets the Comments microservice resolve comment author
 * profiles. Not part of the public API; secured only for the internal network.
 */
@RestController
@RequestMapping(path = "/internal/profiles")
@AllArgsConstructor
public class InternalProfileApi {

  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping(path = "/{userId}")
  public ResponseEntity<InternalProfile> getProfile(
      @PathVariable("userId") String userId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    boolean following =
        viewerId != null
            && !viewerId.isEmpty()
            && userRelationshipQueryService.isUserFollowing(viewerId, userId);
    return ResponseEntity.ok(
        new InternalProfile(
            userData.getId(),
            userData.getUsername(),
            userData.getBio(),
            userData.getImage(),
            following));
  }

  @Data
  @AllArgsConstructor
  public static class InternalProfile {
    private String id;
    private String username;
    private String bio;
    private String image;
    private boolean following;
  }
}
