package io.spring.articleservice.api;

import io.spring.articleservice.application.article.NewArticleParam;
import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ArticleDataList;
import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleRepository articleRepository;
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;

  @PostMapping
  public ResponseEntity<?> createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal String userId) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList() == null ? List.of() : newArticleParam.getTagList(),
            userId);
    articleRepository.save(article);
    ArticleData articleData = articleReadService.findById(article.getId());
    return ResponseEntity.ok(articleResponse(articleData));
  }

  @GetMapping
  public ResponseEntity<?> getArticles(
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal String userId) {
    List<String> articleIds =
        articleReadService.queryArticles(tag, author, favoritedBy, offset, limit);
    int count = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.isEmpty()) {
      return ResponseEntity.ok(new ArticleDataList(new ArrayList<>(), count));
    }
    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    if (userId != null) {
      fillFavoriteInfo(articles, userId);
    }
    return ResponseEntity.ok(new ArticleDataList(articles, count));
  }

  @GetMapping(path = "feed")
  public ResponseEntity<?> getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(new ArticleDataList(new ArrayList<>(), 0));
  }

  private void fillFavoriteInfo(List<ArticleData> articles, String userId) {
    List<String> ids = articles.stream().map(ArticleData::getId).toList();
    var favorites = articleFavoritesReadService.userFavorites(ids, userId);
    var counts = articleFavoritesReadService.articlesFavoriteCount(ids);
    Map<String, Integer> countMap = new HashMap<>();
    counts.forEach(c -> countMap.put(c.getId(), c.getCount()));
    for (ArticleData a : articles) {
      a.setFavorited(favorites.contains(a.getId()));
      a.setFavoritesCount(countMap.getOrDefault(a.getId(), 0));
    }
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<>() {
      {
        put("article", articleData);
      }
    };
  }
}
