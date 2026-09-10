package io.spring.api.internal;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only endpoints consumed by extracted services. Disabled unless {@code
 * internal.api.enabled=true} so the public surface of the monolith is unchanged by default.
 */
@RestController
@RequestMapping(path = "/internal")
@ConditionalOnProperty(name = "internal.api.enabled", havingValue = "true")
@AllArgsConstructor
public class InternalApi {
  private final ArticleQueryService articleQueryService;
  private final UserRepository userRepository;
  private final UserReadService userReadService;
  private final UserRelationshipQueryService userRelationshipQueryService;

  @GetMapping("/articles/{slug}")
  public ResponseEntity<Map<String, Object>> articleBySlug(
      @PathVariable("slug") String slug,
      @RequestParam(name = "viewerId", required = false) String viewerId) {
    ArticleData article =
        articleQueryService
            .findBySlug(slug, viewer(viewerId))
            .orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(single("article", toArticleDto(article)));
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<Map<String, Object>> userById(
      @PathVariable("userId") String userId,
      @RequestParam(name = "viewerId", required = false) String viewerId) {
    UserData userData = userReadService.findById(userId);
    if (userData == null) {
      throw new ResourceNotFoundException();
    }
    boolean following =
        viewerId != null && userRelationshipQueryService.isUserFollowing(viewerId, userId);
    return ResponseEntity.ok(single("profile", toProfileDto(userData, following)));
  }

  @GetMapping("/users")
  public ResponseEntity<Map<String, Object>> usersByIds(
      @RequestParam("ids") List<String> ids,
      @RequestParam(name = "viewerId", required = false) String viewerId) {
    Set<String> followed =
        viewerId == null || ids.isEmpty()
            ? Collections.emptySet()
            : userRelationshipQueryService.followingAuthors(viewerId, ids);
    List<InternalProfileDto> profiles = new ArrayList<>();
    for (String id : ids) {
      UserData userData = userReadService.findById(id);
      if (userData != null) {
        profiles.add(toProfileDto(userData, followed.contains(id)));
      }
    }
    return ResponseEntity.ok(single("profiles", profiles));
  }

  private User viewer(String viewerId) {
    return viewerId == null ? null : userRepository.findById(viewerId).orElse(null);
  }

  private Map<String, Object> single(String key, Object value) {
    Map<String, Object> body = new HashMap<>();
    body.put(key, value);
    return body;
  }

  private InternalProfileDto toProfileDto(UserData userData, boolean following) {
    return new InternalProfileDto(
        userData.getId(),
        userData.getUsername(),
        userData.getBio(),
        userData.getImage(),
        following);
  }

  private InternalArticleDto toArticleDto(ArticleData article) {
    ProfileData author = article.getProfileData();
    return new InternalArticleDto(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        article.getCreatedAt(),
        article.getUpdatedAt(),
        article.getTagList(),
        new InternalProfileDto(
            author.getId(),
            author.getUsername(),
            author.getBio(),
            author.getImage(),
            author.isFollowing()));
  }
}
