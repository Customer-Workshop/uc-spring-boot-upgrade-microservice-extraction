package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.client.UserServiceClient;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.common.data.ProfileData;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    enrichProfile(commentData);
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    comments.forEach(this::enrichProfile);
    return comments;
  }

  private void enrichProfile(CommentData commentData) {
    if (commentData.getUserId() != null) {
      Optional<ProfileData> profile = userServiceClient.getProfileByUserId(commentData.getUserId());
      profile.ifPresent(commentData::setProfileData);
    }
  }
}
