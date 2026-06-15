package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.client.UserServiceClient;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
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
    List<String> followedUsers = userServiceClient.getFollowedUserIds(userId);
    if (followedUsers.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followedUsers, page);
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
    List<String> followedUsers = userServiceClient.getFollowedUserIds(userId);
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
    fillProfilesData(articles);
    if (currentUserId != null) {
      setIsFavorite(articles, currentUserId);
      setIsFollowingAuthor(articles, currentUserId);
    }
  }

  private void fillProfileData(ArticleData articleData) {
    if (articleData.getProfileData() != null && articleData.getProfileData().getId() != null) {
      ProfileData profile = userServiceClient.getProfile(articleData.getProfileData().getId());
      articleData.setProfileData(profile);
    }
  }

  private void fillProfilesData(List<ArticleData> articles) {
    List<String> userIds =
        articles.stream()
            .filter(a -> a.getProfileData() != null && a.getProfileData().getId() != null)
            .map(a -> a.getProfileData().getId())
            .distinct()
            .collect(toList());
    if (userIds.isEmpty()) {
      return;
    }
    Map<String, ProfileData> profiles = userServiceClient.getProfiles(userIds);
    articles.forEach(
        articleData -> {
          if (articleData.getProfileData() != null
              && articleData.getProfileData().getId() != null) {
            ProfileData profile = profiles.get(articleData.getProfileData().getId());
            if (profile != null) {
              articleData.setProfileData(profile);
            }
          }
        });
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, String currentUserId) {
    Set<String> followingAuthors =
        userServiceClient.getFollowingAuthors(
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
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }

  private void setIsFavorite(List<ArticleData> articles, String currentUserId) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(articleData -> articleData.getId()).collect(toList()),
            currentUserId);

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
        .setFollowing(userServiceClient.isFollowing(userId, articleData.getProfileData().getId()));
  }
}
