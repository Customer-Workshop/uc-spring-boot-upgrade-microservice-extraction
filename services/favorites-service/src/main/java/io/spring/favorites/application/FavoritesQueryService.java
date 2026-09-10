package io.spring.favorites.application;

import io.spring.favorites.application.data.ArticleData;
import io.spring.favorites.application.data.ArticleFavoriteCount;
import io.spring.favorites.application.data.ArticleFavoriteData;
import io.spring.favorites.application.data.ProfileData;
import io.spring.favorites.infrastructure.monolith.dto.ArticleDto;
import io.spring.favorites.infrastructure.monolith.dto.ProfileDto;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Combines the article envelope echoed by the monolith with this service's favorite state. */
@Service
public class FavoritesQueryService {
  private final ArticleFavoritesReadService articleFavoritesReadService;

  public FavoritesQueryService(ArticleFavoritesReadService articleFavoritesReadService) {
    this.articleFavoritesReadService = articleFavoritesReadService;
  }

  public ArticleData articleData(ArticleDto article, String viewerId) {
    boolean favorited =
        viewerId != null && articleFavoritesReadService.isUserFavorite(viewerId, article.getId());
    int favoritesCount = articleFavoritesReadService.articleFavoriteCount(article.getId());
    return new ArticleData(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        favorited,
        favoritesCount,
        article.getCreatedAt(),
        article.getUpdatedAt(),
        article.getTagList(),
        profileData(article.getAuthor()));
  }

  public List<ArticleFavoriteData> favoritesOf(List<String> articleIds, String viewerId) {
    if (articleIds == null || articleIds.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, Integer> counts = new HashMap<>();
    for (ArticleFavoriteCount count :
        articleFavoritesReadService.articlesFavoriteCount(articleIds)) {
      counts.put(count.getId(), count.getCount());
    }
    Set<String> favorited =
        viewerId == null || viewerId.isEmpty()
            ? Collections.emptySet()
            : articleFavoritesReadService.userFavorites(articleIds, viewerId);
    List<ArticleFavoriteData> result = new ArrayList<>();
    for (String articleId : articleIds) {
      result.add(
          new ArticleFavoriteData(
              articleId,
              counts.getOrDefault(articleId, 0),
              favorited != null && favorited.contains(articleId)));
    }
    return result;
  }

  private ProfileData profileData(ProfileDto author) {
    if (author == null) {
      return null;
    }
    return new ProfileData(
        author.getId(),
        author.getUsername(),
        author.getBio(),
        author.getImage(),
        author.isFollowing());
  }
}
