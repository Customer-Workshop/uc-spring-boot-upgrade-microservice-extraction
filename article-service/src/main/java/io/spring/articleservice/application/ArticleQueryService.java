package io.spring.articleservice.application;

import static java.util.stream.Collectors.toList;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.ArticleFavoriteCount;
import io.spring.articleservice.infrastructure.client.UserProfileResponse;
import io.spring.articleservice.infrastructure.client.UserServiceClient;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;
  private ArticleFavoritesReadService articleFavoritesReadService;

  public Optional<ArticleData> findById(String id, String userId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillProfileData(articleData);
      if (userId != null) {
        fillExtraInfo(id, userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, String userId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillProfileData(articleData);
      if (userId != null) {
        fillExtraInfo(articleData.getId(), userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String currentUserId) {
    String authorUserId = resolveUsernameToUserId(author);
    String favoritedByUserId = resolveUsernameToUserId(favoritedBy);
    List<String> articleIds =
        articleReadService.queryArticles(tag, authorUserId, favoritedByUserId, page);
    int articleCount = articleReadService.countArticle(tag, authorUserId, favoritedByUserId);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUserId);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(String userId, Page page) {
    List<String> followedUsers = userServiceClient.followedUsers(userId);
    if (followedUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
      fillExtraInfo(articles, userId);
      int count = articleReadService.countFeedSize(followedUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    setFavoriteCount(articles);
    fillProfilesForArticles(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
      setIsFollowingAuthor(articles, currentUserId);
    }
  }

  private void fillProfilesForArticles(List<ArticleData> articles) {
    List<String> userIds =
        articles.stream()
            .map(a -> a.getProfileData() != null ? a.getProfileData().getId() : null)
            .filter(id -> id != null)
            .distinct()
            .collect(toList());

    if (!userIds.isEmpty()) {
      Map<String, UserProfileResponse> profiles = userServiceClient.getUserProfiles(userIds);
      articles.forEach(
          articleData -> {
            if (articleData.getProfileData() != null) {
              UserProfileResponse profile = profiles.get(articleData.getProfileData().getId());
              if (profile != null) {
                articleData.getProfileData().setUsername(profile.getUsername());
                articleData.getProfileData().setBio(profile.getBio());
                articleData.getProfileData().setImage(profile.getImage());
              }
            }
          });
    }
  }

  private void fillProfileData(ArticleData articleData) {
    if (articleData.getProfileData() != null && articleData.getProfileData().getId() != null) {
      userServiceClient
          .getUserProfile(articleData.getProfileData().getId())
          .ifPresent(
              profile -> {
                articleData.getProfileData().setUsername(profile.getUsername());
                articleData.getProfileData().setBio(profile.getBio());
                articleData.getProfileData().setImage(profile.getImage());
              });
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String currentUserId) {
    Set<String> followingAuthors =
        userServiceClient.followingAuthors(
            currentUserId,
            articles.stream()
                .map(articleData -> articleData.getProfileData().getId())
                .collect(toList()));
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(
        item -> {
          countMap.put(item.getId(), item.getCount());
        });
    articles.forEach(
        articleData ->
            articleData.setFavoritesCount(countMap.getOrDefault(articleData.getId(), 0)));
  }

  private void setIsFavorite(List<ArticleData> articles, String currentUserId) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()), currentUserId);

    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void fillExtraInfo(String id, String userId, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(userId, id));
    articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
    articleData
        .getProfileData()
        .setFollowing(
            userServiceClient.isUserFollowing(userId, articleData.getProfileData().getId()));
  }

  private String resolveUsernameToUserId(String username) {
    if (username == null) {
      return null;
    }
    return userServiceClient.getUserIdByUsername(username).orElse(username);
  }
}
