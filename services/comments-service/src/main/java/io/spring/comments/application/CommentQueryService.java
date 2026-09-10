package io.spring.comments.application;

import io.spring.comments.api.exception.MonolithUnavailableException;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.application.data.ProfileData;
import io.spring.comments.core.user.CurrentUser;
import io.spring.comments.infrastructure.monolith.MonolithClient;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import io.spring.comments.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private MonolithClient monolithClient;

  public Optional<CommentData> findById(String id, CurrentUser user) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    String authorId = commentData.getProfileData().getId();
    ProfileDTO profile =
        monolithClient.findProfileById(authorId, user == null ? null : user.getId());
    commentData.setProfileData(toProfileData(profile));
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, CurrentUser user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    fillProfiles(comments, user);
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, CurrentUser user, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    fillProfiles(comments, user);
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private void fillProfiles(List<CommentData> comments, CurrentUser user) {
    if (comments.isEmpty()) {
      return;
    }
    Set<String> authorIds =
        comments.stream()
            .map(commentData -> commentData.getProfileData().getId())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    Map<String, ProfileDTO> profiles =
        monolithClient.findProfilesByIds(authorIds, user == null ? null : user.getId()).stream()
            .collect(
                Collectors.toMap(ProfileDTO::getId, Function.identity(), (first, second) -> first));
    comments.forEach(
        commentData -> {
          ProfileDTO profile = profiles.get(commentData.getProfileData().getId());
          if (profile == null) {
            throw new MonolithUnavailableException(
                "/internal/users?ids=" + String.join(",", authorIds));
          }
          commentData.setProfileData(toProfileData(profile));
        });
  }

  private ProfileData toProfileData(ProfileDTO profile) {
    return new ProfileData(
        profile.getId(),
        profile.getUsername(),
        profile.getBio(),
        profile.getImage(),
        profile.isFollowing());
  }
}
