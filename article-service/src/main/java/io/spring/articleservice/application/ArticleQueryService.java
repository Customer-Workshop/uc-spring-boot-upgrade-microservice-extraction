package io.spring.articleservice.application;

import static java.util.stream.Collectors.toList;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.ArticleFavoriteCount;
import io.spring.articleservice.infrastructure.client.UserServiceClient;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
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
      if (userId != null) {
        fillExtraInfo(articleData.getId(), userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      String currentUserId) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      boolean hasExtra = articleIds.size() > page.getLimit();
      if (hasExtra) {
        articleIds.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articleIds);
      }

      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUserId);

      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public CursorPager<ArticleData> findUserFeedWithCursor(
      String userId, CursorPageParameter<DateTime> page) {
    List<String> followdUsers = userServiceClient.followedUsers(userId);
    if (followdUsers.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followdUsers, page);
      boolean hasExtra = articles.size() > page.getLimit();
      if (hasExtra) {
        articles.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articles);
      }
      fillExtraInfo(articles, userId);
      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String currentUserId) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUserId);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(String userId, Page page) {
    List<String> followdUsers = userServiceClient.followedUsers(userId);
    if (followdUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
      fillExtraInfo(articles, userId);
      int count = articleReadService.countFeedSize(followdUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String currentUserId) {
    setFavoriteCount(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
      setIsFollowingAuthor(articles, currentUserId);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String currentUserId) {
    Set<String> followingAuthors =
        userServiceClient.followingAuthors(
            currentUserId,
            articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
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
}
