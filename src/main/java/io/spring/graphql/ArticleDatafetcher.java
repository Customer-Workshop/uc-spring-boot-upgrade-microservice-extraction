package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.ARTICLEPAYLOAD;
import io.spring.graphql.DgsConstants.COMMENT;
import io.spring.graphql.DgsConstants.PROFILE;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticleEdge;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@DgsComponent
@AllArgsConstructor
public class ArticleDatafetcher {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;

  @DgsQuery(field = QUERY.Feed)
  public DataFetcherResult<ArticlesConnection> getFeed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {
    User current = SecurityUtil.getCurrentUser().orElse(null);
    CursorPager<ArticleData> articles =
        fetchArticlesCursor(
            first,
            after,
            last,
            before,
            (cursor, limit, direction) ->
                articleQueryService.findUserFeedWithCursor(
                    current, new CursorPageParameter<>(cursor, limit, direction)));
    return buildConnectionResult(articles);
  }

  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Feed)
  public DataFetcherResult<ArticlesConnection> userFeed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {
    Profile profile = dfe.getSource();
    User target =
        userRepository
            .findByUsername(profile.getUsername())
            .orElseThrow(ResourceNotFoundException::new);
    CursorPager<ArticleData> articles =
        fetchArticlesCursor(
            first,
            after,
            last,
            before,
            (cursor, limit, direction) ->
                articleQueryService.findUserFeedWithCursor(
                    target, new CursorPageParameter<>(cursor, limit, direction)));
    return buildConnectionResult(articles);
  }

  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Favorites)
  public DataFetcherResult<ArticlesConnection> userFavorites(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {
    User current = SecurityUtil.getCurrentUser().orElse(null);
    Profile profile = dfe.getSource();
    CursorPager<ArticleData> articles =
        fetchArticlesCursor(
            first,
            after,
            last,
            before,
            (cursor, limit, direction) ->
                articleQueryService.findRecentArticlesWithCursor(
                    null,
                    null,
                    profile.getUsername(),
                    new CursorPageParameter<>(cursor, limit, direction),
                    current));
    return buildConnectionResult(articles);
  }

  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Articles)
  public DataFetcherResult<ArticlesConnection> userArticles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {
    User current = SecurityUtil.getCurrentUser().orElse(null);
    Profile profile = dfe.getSource();
    CursorPager<ArticleData> articles =
        fetchArticlesCursor(
            first,
            after,
            last,
            before,
            (cursor, limit, direction) ->
                articleQueryService.findRecentArticlesWithCursor(
                    null,
                    profile.getUsername(),
                    null,
                    new CursorPageParameter<>(cursor, limit, direction),
                    current));
    return buildConnectionResult(articles);
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
    User current = SecurityUtil.getCurrentUser().orElse(null);
    CursorPager<ArticleData> articles =
        fetchArticlesCursor(
            first,
            after,
            last,
            before,
            (cursor, limit, direction) ->
                articleQueryService.findRecentArticlesWithCursor(
                    withTag,
                    authoredBy,
                    favoritedBy,
                    new CursorPageParameter<>(cursor, limit, direction),
                    current));
    return buildConnectionResult(articles);
  }

  @DgsData(parentType = ARTICLEPAYLOAD.TYPE_NAME, field = ARTICLEPAYLOAD.Article)
  public DataFetcherResult<Article> getArticle(DataFetchingEnvironment dfe) {
    io.spring.core.article.Article article = dfe.getLocalContext();

    User current = SecurityUtil.getCurrentUser().orElse(null);
    ArticleData articleData =
        articleQueryService
            .findById(article.getId(), current)
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

  @DgsData(parentType = COMMENT.TYPE_NAME, field = COMMENT.Article)
  public DataFetcherResult<Article> getCommentArticle(
      DataFetchingEnvironment dataFetchingEnvironment) {
    CommentData comment = dataFetchingEnvironment.getLocalContext();
    User current = SecurityUtil.getCurrentUser().orElse(null);
    ArticleData articleData =
        articleQueryService
            .findById(comment.getArticleId(), current)
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
    User current = SecurityUtil.getCurrentUser().orElse(null);
    ArticleData articleData =
        articleQueryService.findBySlug(slug, current).orElseThrow(ResourceNotFoundException::new);
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

  @FunctionalInterface
  private interface CursorFetcher {
    CursorPager<ArticleData> fetch(DateTime cursor, int limit, CursorPager.Direction direction);
  }

  private CursorPager<ArticleData> fetchArticlesCursor(
      Integer first, String after, Integer last, String before, CursorFetcher fetcher) {
    if (first == null && last == null) {
      throw new IllegalArgumentException("first 和 last 必须只存在一个");
    }
    if (first != null) {
      return fetcher.fetch(DateTimeCursor.parse(after), first, Direction.NEXT);
    }
    return fetcher.fetch(DateTimeCursor.parse(before), last, Direction.PREV);
  }

  private DataFetcherResult<ArticlesConnection> buildConnectionResult(
      CursorPager<ArticleData> articles) {
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
