package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_create_and_save_article() {
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(newArticleParam, user);

    assertThat(article, notNullValue());
    assertThat(article.getTitle(), is("a new title"));
    assertThat(article.getSlug(), is("a-new-title"));
    assertThat(article.getDescription(), is("desc"));
    assertThat(article.getBody(), is("body"));
    assertThat(article.getTags().size(), is(2));
    assertThat(article.getUserId(), is(user.getId()));

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue(), is(article));
    assertThat(captor.getValue().getTitle(), is("a new title"));
  }

  @Test
  public void should_update_and_save_article() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateArticleParam =
        new UpdateArticleParam("new title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, updateArticleParam);

    assertThat(updated, notNullValue());
    assertThat(updated, is(article));
    assertThat(updated.getTitle(), is("new title"));
    assertThat(updated.getSlug(), is("new-title"));
    assertThat(updated.getDescription(), is("new desc"));
    assertThat(updated.getBody(), is("new body"));

    verify(articleRepository, times(1)).save(article);
  }

  @Test
  public void should_keep_article_unchanged_when_update_param_is_empty() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateArticleParam = new UpdateArticleParam("", "", "");

    Article updated = articleCommandService.updateArticle(article, updateArticleParam);

    assertThat(updated.getTitle(), is("old title"));
    assertThat(updated.getDescription(), is("old desc"));
    assertThat(updated.getBody(), is("old body"));
    verify(articleRepository, times(1)).save(any(Article.class));
  }
}
