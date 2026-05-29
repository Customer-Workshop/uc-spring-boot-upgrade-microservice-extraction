package io.spring.articleservice.service;

import io.spring.articleservice.client.UserServiceClient;
import io.spring.articleservice.dto.CommentDto;
import io.spring.articleservice.dto.ProfileDto;
import io.spring.articleservice.exception.ForbiddenException;
import io.spring.articleservice.exception.ResourceNotFoundException;
import io.spring.articleservice.model.Article;
import io.spring.articleservice.model.Comment;
import io.spring.articleservice.repository.ArticleRepository;
import io.spring.articleservice.repository.CommentRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final ArticleRepository articleRepository;
  private final UserServiceClient userServiceClient;

  @Transactional
  public CommentDto createComment(String slug, String body, String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(body, userId, article.getId());
    comment = commentRepository.save(comment);
    return toDto(comment, userId);
  }

  @Transactional(readOnly = true)
  public List<CommentDto> getCommentsBySlug(String slug, String currentUserId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    List<Comment> comments =
        commentRepository.findByArticleIdOrderByCreatedAtDesc(article.getId());

    if (comments.isEmpty()) {
      return List.of();
    }

    List<String> authorIds =
        comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
    Map<String, ProfileDto> profileMap = userServiceClient.getProfilesByIds(authorIds);

    Set<String> followingAuthors =
        currentUserId != null
            ? userServiceClient.getFollowingAuthors(currentUserId, authorIds)
            : Set.of();

    return comments.stream()
        .map(
            comment -> {
              ProfileDto profile = profileMap.get(comment.getUserId());
              boolean isFollowing =
                  profile != null && followingAuthors.contains(profile.id());
              ProfileDto enrichedProfile =
                  profile != null
                      ? new ProfileDto(
                          profile.id(),
                          profile.username(),
                          profile.bio(),
                          profile.image(),
                          isFollowing)
                      : new ProfileDto(comment.getUserId(), "unknown", null, null, false);
              return new CommentDto(
                  comment.getId(),
                  comment.getBody(),
                  comment.getCreatedAt(),
                  comment.getUpdatedAt(),
                  enrichedProfile);
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteComment(String slug, String commentId, String currentUserId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment =
        commentRepository
            .findByIdAndArticleId(commentId, article.getId())
            .orElseThrow(ResourceNotFoundException::new);

    boolean isArticleOwner = article.getUserId().equals(currentUserId);
    boolean isCommentOwner = comment.getUserId().equals(currentUserId);
    if (!isArticleOwner && !isCommentOwner) {
      throw new ForbiddenException();
    }

    commentRepository.delete(comment);
  }

  private CommentDto toDto(Comment comment, String currentUserId) {
    ProfileDto profile =
        userServiceClient
            .getProfileById(comment.getUserId())
            .orElse(new ProfileDto(comment.getUserId(), "unknown", null, null, false));

    boolean isFollowing = false;
    if (currentUserId != null) {
      Set<String> following =
          userServiceClient.getFollowingAuthors(currentUserId, List.of(profile.id()));
      isFollowing = following.contains(profile.id());
    }

    ProfileDto enrichedProfile =
        new ProfileDto(profile.id(), profile.username(), profile.bio(), profile.image(), isFollowing);

    return new CommentDto(
        comment.getId(),
        comment.getBody(),
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        enrichedProfile);
  }
}
