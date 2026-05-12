package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class BookmarkMutation {

  private ArticleBookmarkRepository articleBookmarkRepository;
  private ArticleRepository articleRepository;

  @DgsMutation
  public DataFetcherResult<ArticlePayload> bookmarkArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleBookmark bookmark = new ArticleBookmark(article.getId(), user.getId());
    articleBookmarkRepository.save(bookmark);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation
  public DataFetcherResult<ArticlePayload> unbookmarkArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleBookmarkRepository
        .find(article.getId(), user.getId())
        .ifPresent(bookmark -> articleBookmarkRepository.remove(bookmark));
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }
}
