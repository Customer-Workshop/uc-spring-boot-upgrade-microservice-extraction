package io.spring.article.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.article.api.exception.NoAuthorizationException;
import io.spring.article.api.exception.ResourceNotFoundException;
import io.spring.article.application.article.ArticleCommandService;
import io.spring.article.application.article.NewArticleParam;
import io.spring.article.application.article.UpdateArticleParam;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.service.AuthorizationService;
import io.spring.article.graphql.DgsConstants.MUTATION;
import io.spring.article.graphql.exception.AuthenticationException;
import io.spring.article.graphql.types.ArticlePayload;
import io.spring.article.graphql.types.CreateArticleInput;
import io.spring.article.graphql.types.DeletionStatus;
import io.spring.article.graphql.types.UpdateArticleInput;
import java.util.Collections;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ArticleMutation {

  private ArticleCommandService articleCommandService;
  private ArticleRepository articleRepository;

  @DgsMutation(field = MUTATION.CreateArticle)
  public DataFetcherResult<ArticlePayload> createArticle(
      @InputArgument("input") CreateArticleInput input) {
    String userId = SecurityUtil.getCurrentUserId().orElseThrow(AuthenticationException::new);
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .body(input.getBody())
            .tagList(input.getTagList() == null ? Collections.emptyList() : input.getTagList())
            .build();
    Article article = articleCommandService.createArticle(newArticleParam, userId);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.UpdateArticle)
  public DataFetcherResult<ArticlePayload> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput params) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    String userId = SecurityUtil.getCurrentUserId().orElseThrow(AuthenticationException::new);
    if (!AuthorizationService.canWriteArticle(userId, article)) {
      throw new NoAuthorizationException();
    }
    article =
        articleCommandService.updateArticle(
            article,
            new UpdateArticleParam(params.getTitle(), params.getBody(), params.getDescription()));
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.DeleteArticle)
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    String userId = SecurityUtil.getCurrentUserId().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!AuthorizationService.canWriteArticle(userId, article)) {
      throw new NoAuthorizationException();
    }

    articleRepository.remove(article);
    return DeletionStatus.newBuilder().success(true).build();
  }
}
