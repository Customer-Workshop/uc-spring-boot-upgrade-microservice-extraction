package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article_and_save_it() {
    User creator = new User("user@email.com", "username", "123", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("new title")
            .description("new desc")
            .body("new body")
            .tagList(Arrays.asList("java"))
            .build();

    Article created = articleCommandService.createArticle(param, creator);

    assertNotNull(created);
    assertThat(created.getTitle(), is("new title"));
    assertThat(created.getDescription(), is("new desc"));
    assertThat(created.getBody(), is("new body"));
    assertThat(created.getUserId(), is(creator.getId()));
    assertThat(created.getTags().size(), is(1));
    assertThat(created.getTags().get(0).getName(), is("java"));
    verify(articleRepository).save(created);
  }

  @Test
  public void should_update_article_and_save_it() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");
    UpdateArticleParam param = new UpdateArticleParam("new title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertSame(article, updated);
    assertThat(updated.getTitle(), is("new title"));
    assertThat(updated.getSlug(), is("new-title"));
    assertThat(updated.getDescription(), is("new desc"));
    assertThat(updated.getBody(), is("new body"));
    verify(articleRepository).save(article);
  }
}
