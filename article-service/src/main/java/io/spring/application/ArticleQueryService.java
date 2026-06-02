package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.core.service.UserDto;
import io.spring.core.service.UserServiceClient;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;

  public ArticleQueryService(
      ArticleReadService articleReadService, UserServiceClient userServiceClient) {
    this.articleReadService = articleReadService;
    this.userServiceClient = userServiceClient;
  }

  public Optional<ArticleData> findById(String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    fillAuthorProfile(articleData);
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    fillAuthorProfile(articleData);
    return Optional.of(articleData);
  }

  public ArticleDataList findRecentArticles(String tag, String author, Page page) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, page);
    int articleCount = articleReadService.countArticle(tag, author);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      articles.forEach(this::fillAuthorProfile);
      return new ArticleDataList(articles, articleCount);
    }
  }

  private void fillAuthorProfile(ArticleData articleData) {
    if (articleData.getUserId() == null) {
      return;
    }
    Optional<UserDto> userOpt = userServiceClient.findUserById(articleData.getUserId());
    if (userOpt.isPresent()) {
      UserDto user = userOpt.get();
      articleData.setProfileData(
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    } else {
      articleData.setProfileData(
          new ProfileData(articleData.getUserId(), "unknown", null, null, false));
    }
  }
}
