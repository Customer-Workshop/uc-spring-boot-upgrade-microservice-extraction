package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User creator;

  @BeforeEach
  public void setUp() {
    articleRepository = Mockito.mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    creator = new User("test@test.com", "test", "123", "", "");
  }

  @Test
  public void should_create_and_save_article_from_param() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article created = articleCommandService.createArticle(param, creator);

    assertThat(created, notNullValue());
    assertThat(created.getTitle(), is("a new title"));
    assertThat(created.getSlug(), is("a-new-title"));
    assertThat(created.getDescription(), is("desc"));
    assertThat(created.getBody(), is("body"));
    assertThat(created.getUserId(), is(creator.getId()));
    assertThat(created.getTags().size(), is(2));

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(captor.capture());
    assertThat(captor.getValue().getId(), is(created.getId()));
  }

  @Test
  public void should_update_and_save_existing_article() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("new title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertThat(updated, notNullValue());
    assertThat(updated.getTitle(), is("new title"));
    assertThat(updated.getSlug(), is("new-title"));
    assertThat(updated.getDescription(), is("new desc"));
    assertThat(updated.getBody(), is("new body"));

    verify(articleRepository).save(article);
  }
}
