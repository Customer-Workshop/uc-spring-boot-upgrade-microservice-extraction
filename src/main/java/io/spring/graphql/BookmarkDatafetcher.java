package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticleEdge;
import io.spring.graphql.types.ArticlesConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;

@DgsComponent
@AllArgsConstructor
public class BookmarkDatafetcher {

  private ArticleBookmarkRepository articleBookmarkRepository;
  private ArticleQueryService articleQueryService;

  @DgsQuery(field = "bookmarkedArticles")
  public DataFetcherResult<ArticlesConnection> getBookmarkedArticles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);

    List<String> articleIds = articleBookmarkRepository.findBookmarkedArticleIds(user.getId());
    List<ArticleData> allArticles =
        articleIds.stream()
            .map(id -> articleQueryService.findById(id, user).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    int start = 0;
    int end = allArticles.size();
    boolean hasNext = false;
    boolean hasPrevious = false;

    if (first != null) {
      int afterIndex = 0;
      if (after != null) {
        for (int i = 0; i < allArticles.size(); i++) {
          if (allArticles.get(i).getSlug().equals(after)) {
            afterIndex = i + 1;
            break;
          }
        }
      }
      start = afterIndex;
      end = Math.min(start + first, allArticles.size());
      hasNext = end < allArticles.size();
      hasPrevious = start > 0;
    } else if (last != null) {
      int beforeIndex = allArticles.size();
      if (before != null) {
        for (int i = 0; i < allArticles.size(); i++) {
          if (allArticles.get(i).getSlug().equals(before)) {
            beforeIndex = i;
            break;
          }
        }
      }
      start = Math.max(beforeIndex - last, 0);
      end = beforeIndex;
      hasNext = end < allArticles.size();
      hasPrevious = start > 0;
    }

    List<ArticleData> pageData = start < end ? allArticles.subList(start, end) : new ArrayList<>();

    graphql.relay.PageInfo pageInfo =
        new DefaultPageInfo(
            pageData.isEmpty() ? null : new DefaultConnectionCursor(pageData.get(0).getSlug()),
            pageData.isEmpty()
                ? null
                : new DefaultConnectionCursor(pageData.get(pageData.size() - 1).getSlug()),
            hasPrevious,
            hasNext);

    ArticlesConnection connection =
        ArticlesConnection.newBuilder()
            .pageInfo(pageInfo)
            .edges(
                pageData.stream()
                    .map(
                        a ->
                            ArticleEdge.newBuilder()
                                .cursor(a.getSlug())
                                .node(buildArticleResult(a))
                                .build())
                    .collect(Collectors.toList()))
            .build();

    return DataFetcherResult.<ArticlesConnection>newResult()
        .data(connection)
        .localContext(pageData.stream().collect(Collectors.toMap(ArticleData::getSlug, a -> a)))
        .build();
  }

  @DgsData(parentType = "Article", field = "bookmarked")
  public boolean isBookmarked(DataFetchingEnvironment dfe) {
    Article article = dfe.getSource();
    User user = SecurityUtil.getCurrentUser().orElse(null);
    if (user == null) {
      return false;
    }
    return articleBookmarkRepository.find(article.getSlug(), user.getId()).isPresent();
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
