package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.client.UserProfileDto;
import io.spring.infrastructure.client.UserServiceClient;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
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

  public ArticleDataList findRecentArticles(
      String tag, String author, Page page, String currentUserId) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, page);
    int articleCount = articleReadService.countArticle(tag, author);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillAuthorProfiles(articles);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(List<String> followedUsers, Page page) {
    if (followedUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
      fillAuthorProfiles(articles);
      int count = articleReadService.countFeedSize(followedUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillAuthorProfiles(List<ArticleData> articles) {
    articles.forEach(this::fillAuthorProfile);
  }

  private void fillAuthorProfile(ArticleData articleData) {
    String authorUserId = articleData.getUserId();
    if (authorUserId == null) {
      articleData.setProfileData(defaultProfile());
      return;
    }
    Optional<UserProfileDto> profile = userServiceClient.findUserById(authorUserId);
    if (profile.isPresent()) {
      UserProfileDto dto = profile.get();
      articleData.setProfileData(
          new ProfileData(dto.getId(), dto.getUsername(), dto.getBio(), dto.getImage(), false));
    } else {
      articleData.setProfileData(defaultProfile());
    }
  }

  private ProfileData defaultProfile() {
    return new ProfileData("", "unknown", "", "", false);
  }
}
