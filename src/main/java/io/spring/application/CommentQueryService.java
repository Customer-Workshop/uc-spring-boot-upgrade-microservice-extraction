package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ProfileQueryService profileQueryService;

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient.getCommentById(id).map(cr -> toCommentData(cr, user));
  }

  public Optional<CommentData> findByIdInArticle(String articleId, String id, User user) {
    return commentServiceClient.getComment(articleId, id).map(cr -> toCommentData(cr, user));
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentResponse> comments = commentServiceClient.getCommentsByArticleId(articleId);
    if (comments.isEmpty()) {
      return Collections.emptyList();
    }
    List<CommentData> result =
        comments.stream().map(cr -> toCommentData(cr, null)).collect(Collectors.toList());
    if (user != null) {
      List<String> authorIds =
          result.stream()
              .filter(cd -> cd.getProfileData() != null)
              .map(cd -> cd.getProfileData().getId())
              .collect(Collectors.toList());
      if (!authorIds.isEmpty()) {
        Set<String> followingAuthors =
            userRelationshipQueryService.followingAuthors(user.getId(), authorIds);
        result.stream()
            .filter(cd -> cd.getProfileData() != null)
            .forEach(
                cd -> {
                  if (followingAuthors.contains(cd.getProfileData().getId())) {
                    cd.getProfileData().setFollowing(true);
                  }
                });
      }
    }
    return result;
  }

  private CommentData toCommentData(CommentResponse cr, User user) {
    ProfileData profileData = profileQueryService.findByUserId(cr.getUserId()).orElse(null);
    if (profileData != null && user != null) {
      profileData.setFollowing(
          userRelationshipQueryService.isUserFollowing(user.getId(), profileData.getId()));
    }
    return new CommentData(
        cr.getId(),
        cr.getBody(),
        cr.getArticleId(),
        cr.getCreatedAt(),
        cr.getUpdatedAt(),
        profileData);
  }
}
