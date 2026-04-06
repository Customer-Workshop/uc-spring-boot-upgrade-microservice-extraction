package io.spring.article.application;

import io.spring.article.application.data.ArticleData;
import io.spring.article.application.data.ArticleDataList;
import io.spring.article.client.InteractionServiceClient;
import io.spring.article.client.UserServiceClient;
import io.spring.article.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;
  private InteractionServiceClient interactionServiceClient;

  public Optional<ArticleData> findById(String id) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    return Optional.of(articleData);
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page) {
    // Resolve author username to userId via User Service
    String authorUserId = null;
    if (author != null) {
      authorUserId = userServiceClient.getUserIdByUsername(author);
      if (authorUserId == null) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
    }

    // Handle favoritedBy via Interaction Service (cross-service query)
    if (favoritedBy != null) {
      String favoritedByUserId = userServiceClient.getUserIdByUsername(favoritedBy);
      if (favoritedByUserId == null) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      List<String> favoritedArticleIds =
          interactionServiceClient.getArticlesFavoritedByUser(favoritedByUserId);
      if (favoritedArticleIds.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      // Filter by tag and author if also specified
      List<String> articleIds;
      if (tag != null || authorUserId != null) {
        List<String> queriedIds =
            articleReadService.queryArticles(tag, authorUserId, null, page);
        articleIds =
            queriedIds.stream()
                .filter(favoritedArticleIds::contains)
                .collect(Collectors.toList());
      } else {
        // Apply pagination to favorited articles
        int offset = page.getOffset();
        int limit = page.getLimit();
        articleIds =
            favoritedArticleIds.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
      }
      if (articleIds.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      return new ArticleDataList(articles, favoritedArticleIds.size());
    }

    List<String> articleIds =
        articleReadService.queryArticles(tag, authorUserId, null, page);
    int articleCount = articleReadService.countArticle(tag, authorUserId, null);
    if (articleIds.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findArticlesOfAuthors(List<String> authors, Page page) {
    if (authors.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }
    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(authors, page);
    int count = articleReadService.countFeedSize(authors);
    return new ArticleDataList(articles, count);
  }
}
