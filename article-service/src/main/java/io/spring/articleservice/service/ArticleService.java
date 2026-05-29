package io.spring.articleservice.service;

import io.spring.articleservice.client.UserServiceClient;
import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.dto.ArticleListDto;
import io.spring.articleservice.dto.ProfileDto;
import io.spring.articleservice.dto.request.NewArticleRequest;
import io.spring.articleservice.dto.request.UpdateArticleRequest;
import io.spring.articleservice.exception.ForbiddenException;
import io.spring.articleservice.exception.ResourceNotFoundException;
import io.spring.articleservice.model.Article;
import io.spring.articleservice.model.Tag;
import io.spring.articleservice.repository.ArticleFavoriteRepository;
import io.spring.articleservice.repository.ArticleRepository;
import io.spring.articleservice.repository.TagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final TagRepository tagRepository;
  private final ArticleFavoriteRepository favoriteRepository;
  private final UserServiceClient userServiceClient;

  @Transactional
  public ArticleDto createArticle(NewArticleRequest request, String userId) {
    Article article = new Article(request.title(), request.description(), request.body(), userId);

    if (request.tagList() != null) {
      Set<Tag> tags = new HashSet<>();
      for (String tagName : request.tagList()) {
        Tag tag = tagRepository.findByName(tagName).orElseGet(() -> tagRepository.save(new Tag(tagName)));
        tags.add(tag);
      }
      article.setTags(tags);
    }

    article = articleRepository.save(article);
    return toDto(article, userId);
  }

  public Optional<ArticleDto> findBySlug(String slug, String currentUserId) {
    return articleRepository.findBySlug(slug).map(article -> toDto(article, currentUserId));
  }

  @Transactional
  public ArticleDto updateArticle(String slug, UpdateArticleRequest request, String currentUserId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!article.getUserId().equals(currentUserId)) {
      throw new ForbiddenException();
    }

    article.update(request.title(), request.description(), request.body());
    article = articleRepository.save(article);
    return toDto(article, currentUserId);
  }

  @Transactional
  public void deleteArticle(String slug, String currentUserId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!article.getUserId().equals(currentUserId)) {
      throw new ForbiddenException();
    }

    articleRepository.delete(article);
  }

  public ArticleListDto listArticles(
      String tag, String author, String favoritedBy, int offset, int limit, String currentUserId) {

    String authorUserId = null;
    if (author != null) {
      Optional<ProfileDto> authorProfile = userServiceClient.getProfileByUsername(author);
      if (authorProfile.isEmpty()) {
        return new ArticleListDto(List.of(), 0);
      }
      authorUserId = authorProfile.get().id();
    }

    String favoritedByUserId = null;
    if (favoritedBy != null) {
      Optional<ProfileDto> favoritedByProfile = userServiceClient.getProfileByUsername(favoritedBy);
      if (favoritedByProfile.isEmpty()) {
        return new ArticleListDto(List.of(), 0);
      }
      favoritedByUserId = favoritedByProfile.get().id();
    }

    List<Article> articles =
        articleRepository.findByAllFilters(tag, authorUserId, favoritedByUserId, offset, limit);
    int count = articleRepository.countByAllFilters(tag, authorUserId, favoritedByUserId);

    List<ArticleDto> dtos = enrichArticles(articles, currentUserId);
    return new ArticleListDto(dtos, count);
  }

  public ArticleListDto getUserFeed(String userId, int offset, int limit) {
    List<String> followedUserIds = userServiceClient.getFollowedUsers(userId);
    if (followedUserIds.isEmpty()) {
      return new ArticleListDto(List.of(), 0);
    }

    List<Article> articles =
        articleRepository.findByUserIdsFeed(followedUserIds, offset, limit);
    int count = articleRepository.countByUserIdIn(followedUserIds);

    List<ArticleDto> dtos = enrichArticles(articles, userId);
    return new ArticleListDto(dtos, count);
  }

  private List<ArticleDto> enrichArticles(List<Article> articles, String currentUserId) {
    if (articles.isEmpty()) {
      return List.of();
    }

    List<String> authorIds =
        articles.stream().map(Article::getUserId).distinct().collect(Collectors.toList());
    Map<String, ProfileDto> profileMap = userServiceClient.getProfilesByIds(authorIds);

    Set<String> followingAuthors =
        currentUserId != null
            ? userServiceClient.getFollowingAuthors(currentUserId, authorIds)
            : Set.of();

    return articles.stream()
        .map(
            article -> {
              ProfileDto profile = profileMap.get(article.getUserId());
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
                      : new ProfileDto(article.getUserId(), "unknown", null, null, false);

              boolean favorited =
                  currentUserId != null
                      && favoriteRepository.existsByArticleIdAndUserId(
                          article.getId(), currentUserId);
              int favoritesCount = favoriteRepository.countByArticleId(article.getId());

              List<String> tagNames =
                  article.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList());

              return new ArticleDto(
                  article.getSlug(),
                  article.getTitle(),
                  article.getDescription(),
                  article.getBody(),
                  tagNames,
                  article.getCreatedAt(),
                  article.getUpdatedAt(),
                  favorited,
                  favoritesCount,
                  enrichedProfile);
            })
        .collect(Collectors.toList());
  }

  ArticleDto toDto(Article article, String currentUserId) {
    ProfileDto profile =
        userServiceClient
            .getProfileById(article.getUserId())
            .orElse(new ProfileDto(article.getUserId(), "unknown", null, null, false));

    boolean isFollowing = false;
    if (currentUserId != null) {
      Set<String> following =
          userServiceClient.getFollowingAuthors(currentUserId, List.of(profile.id()));
      isFollowing = following.contains(profile.id());
    }

    ProfileDto enrichedProfile =
        new ProfileDto(profile.id(), profile.username(), profile.bio(), profile.image(), isFollowing);

    boolean favorited =
        currentUserId != null
            && favoriteRepository.existsByArticleIdAndUserId(article.getId(), currentUserId);
    int favoritesCount = favoriteRepository.countByArticleId(article.getId());

    List<String> tagNames =
        article.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList());

    return new ArticleDto(
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        tagNames,
        article.getCreatedAt(),
        article.getUpdatedAt(),
        favorited,
        favoritesCount,
        enrichedProfile);
  }
}
