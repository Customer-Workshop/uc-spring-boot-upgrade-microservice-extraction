package io.spring.article.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import graphql.schema.DataFetchingEnvironment;
import io.spring.article.api.exception.ResourceNotFoundException;
import io.spring.article.application.ArticleQueryService;
import io.spring.article.application.CursorPageParameter;
import io.spring.article.application.CursorPager;
import io.spring.article.application.CursorPager.Direction;
import io.spring.article.application.DateTimeCursor;
import io.spring.article.application.data.ArticleData;
import io.spring.article.graphql.DgsConstants.ARTICLEPAYLOAD;
import io.spring.article.graphql.DgsConstants.QUERY;
import io.spring.article.graphql.types.Article;
import io.spring.article.graphql.types.ArticleEdge;
import io.spring.article.graphql.types.ArticlesConnection;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;

@DgsComponent
@AllArgsConstructor
public class ArticleDatafetcher {

  private ArticleQueryService articleQueryService;

  @DgsQuery(field = QUERY.Feed)
  public DataFetcherResult<ArticlesConnection> getFeed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {
    if (first == null && last == null) {
      throw new IllegalArgumentException("first and last must have exactly one present");
    }

    String currentUserId = SecurityUtil.getCurrentUserId().orElse(null);

    CursorPager<ArticleData> articles;
    if (first != null) {
      articles =
          articleQueryService.findUserFeedWithCursor(
              currentUserId,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else {
      articles =
          articleQueryService.findUserFeedWithCursor(
              currentUserId,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }
    graphql.relay.PageInfo pageInfo = buildArticlePageInfo(articles);
    ArticlesConnection articlesConnection =
        ArticlesConnection.newBuilder()
            .pageInfo(pageInfo)
            .edges(
                articles.getData().stream()
                    .map(
                        a ->
                            ArticleEdge.newBuilder()
                                .cursor(a.getCursor().toString())
                                .node(buildArticleResult(a))
                                .build())
                    .collect(Collectors.toList()))
            .build();
    return DataFetcherResult.<ArticlesConnection>newResult()
        .data(articlesConnection)
        .localContext(
            articles.getData().stream().collect(Collectors.toMap(ArticleData::getSlug, a -> a)))
        .build();
  }

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Articles)
  public DataFetcherResult<ArticlesConnection> getArticles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      @InputArgument("authoredBy") String authoredBy,
      @InputArgument("favoritedBy") String favoritedBy,
      @InputArgument("withTag") String withTag,
      DgsDataFetchingEnvironment dfe) {
    if (first == null && last == null) {
      throw new IllegalArgumentException("first and last must have exactly one present");
    }

    String currentUserId = SecurityUtil.getCurrentUserId().orElse(null);

    CursorPager<ArticleData> articles;
    if (first != null) {
      articles =
          articleQueryService.findRecentArticlesWithCursor(
              withTag,
              authoredBy,
              favoritedBy,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT),
              currentUserId);
    } else {
      articles =
          articleQueryService.findRecentArticlesWithCursor(
              withTag,
              authoredBy,
              favoritedBy,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV),
              currentUserId);
    }
    graphql.relay.PageInfo pageInfo = buildArticlePageInfo(articles);
    ArticlesConnection articlesConnection =
        ArticlesConnection.newBuilder()
            .pageInfo(pageInfo)
            .edges(
                articles.getData().stream()
                    .map(
                        a ->
                            ArticleEdge.newBuilder()
                                .cursor(a.getCursor().toString())
                                .node(buildArticleResult(a))
                                .build())
                    .collect(Collectors.toList()))
            .build();
    return DataFetcherResult.<ArticlesConnection>newResult()
        .data(articlesConnection)
        .localContext(
            articles.getData().stream().collect(Collectors.toMap(ArticleData::getSlug, a -> a)))
        .build();
  }

  @DgsData(parentType = ARTICLEPAYLOAD.TYPE_NAME, field = ARTICLEPAYLOAD.Article)
  public DataFetcherResult<Article> getArticle(DataFetchingEnvironment dfe) {
    io.spring.article.core.article.Article article = dfe.getLocalContext();

    String currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
    ArticleData articleData =
        articleQueryService
            .findById(article.getId(), currentUserId)
            .orElseThrow(ResourceNotFoundException::new);
    Article articleResult = buildArticleResult(articleData);
    return DataFetcherResult.<Article>newResult()
        .localContext(
            new HashMap<String, Object>() {
              {
                put(articleData.getSlug(), articleData);
              }
            })
        .data(articleResult)
        .build();
  }

  @DgsQuery(field = QUERY.Article)
  public DataFetcherResult<Article> findArticleBySlug(@InputArgument("slug") String slug) {
    String currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
    ArticleData articleData =
        articleQueryService
            .findBySlug(slug, currentUserId)
            .orElseThrow(ResourceNotFoundException::new);
    Article articleResult = buildArticleResult(articleData);
    return DataFetcherResult.<Article>newResult()
        .localContext(
            new HashMap<String, Object>() {
              {
                put(articleData.getSlug(), articleData);
              }
            })
        .data(articleResult)
        .build();
  }

  private DefaultPageInfo buildArticlePageInfo(CursorPager<ArticleData> articles) {
    return new DefaultPageInfo(
        articles.getStartCursor() == null
            ? null
            : new DefaultConnectionCursor(articles.getStartCursor().toString()),
        articles.getEndCursor() == null
            ? null
            : new DefaultConnectionCursor(articles.getEndCursor().toString()),
        articles.hasPrevious(),
        articles.hasNext());
  }

  private Article buildArticleResult(ArticleData articleData) {
    return Article.newBuilder()
        .body(articleData.getBody())
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(articleData.getCreatedAt()))
        .description(articleData.getDescription())
        .favorited(articleData.isFavorited())
        .favoritesCount(articleData.getFavoritesCount())
        .slug(articleData.getSlug())
        .tagList(articleData.getTagList())
        .title(articleData.getTitle())
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(articleData.getUpdatedAt()))
        .build();
  }
}
