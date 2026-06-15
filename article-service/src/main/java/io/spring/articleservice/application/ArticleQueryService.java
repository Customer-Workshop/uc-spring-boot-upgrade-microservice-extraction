package io.spring.articleservice.application;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.infrastructure.client.UserServiceClient;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;

  public Optional<ArticleData> findById(String id, String userId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillAuthorProfile(articleData);
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, String userId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      fillAuthorProfile(articleData);
      return Optional.of(articleData);
    }
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag, String author, CursorPageParameter<DateTime> page, String currentUserId) {
    String authorUserId = resolveAuthorToUserId(author);
    List<String> articleIds = articleReadService.findArticlesWithCursor(tag, authorUserId, page);
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
      fillAuthorProfiles(articles);

      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, Page page, String currentUserId) {
    String authorUserId = resolveAuthorToUserId(author);
    List<String> articleIds = articleReadService.queryArticles(tag, authorUserId, page);
    int articleCount = articleReadService.countArticle(tag, authorUserId);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillAuthorProfiles(articles);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(List<String> followedUserIds, Page page) {
    if (followedUserIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUserIds, page);
      fillAuthorProfiles(articles);
      int count = articleReadService.countFeedSize(followedUserIds);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillAuthorProfiles(List<ArticleData> articles) {
    Set<String> authorIds =
        articles.stream()
            .filter(a -> a.getProfileData() != null && a.getProfileData().getId() != null)
            .map(a -> a.getProfileData().getId())
            .collect(Collectors.toSet());

    Map<String, ProfileData> profileCache =
        authorIds.stream()
            .collect(Collectors.toMap(id -> id, id -> userServiceClient.getProfile(id)));

    articles.forEach(
        articleData -> {
          if (articleData.getProfileData() != null
              && articleData.getProfileData().getId() != null) {
            ProfileData cached = profileCache.get(articleData.getProfileData().getId());
            if (cached != null) {
              articleData.setProfileData(cached);
            }
          }
        });
  }

  private String resolveAuthorToUserId(String author) {
    if (author == null) {
      return null;
    }
    String userId = userServiceClient.resolveUsernameToUserId(author);
    return userId != null ? userId : author;
  }

  private void fillAuthorProfile(ArticleData articleData) {
    if (articleData.getProfileData() != null && articleData.getProfileData().getId() != null) {
      ProfileData profile = userServiceClient.getProfile(articleData.getProfileData().getId());
      articleData.setProfileData(profile);
    }
  }
}
