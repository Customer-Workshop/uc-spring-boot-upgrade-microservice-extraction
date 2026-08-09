package io.spring.application.article;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  @Test
  public void should_create_article_and_save_all_article_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("A New Article")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();
    User creator = new User("author@test.com", "author", "password", "", "");

    Article created = articleCommandService.createArticle(param, creator);

    ArgumentCaptor<Article> savedArticle = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(savedArticle.capture());
    assertThat(savedArticle.getValue(), is(created));
    assertThat(created.getUserId(), is(creator.getId()));
    assertThat(created.getTitle(), is("A New Article"));
    assertThat(created.getSlug(), is("a-new-article"));
    assertThat(created.getDescription(), is("description"));
    assertThat(created.getBody(), is("body"));
    assertThat(created.getTags(), hasItem(new Tag("java")));
    assertThat(created.getTags(), hasItem(new Tag("spring")));
  }

  @Test
  public void should_update_article_and_save_updated_article() {
    Article article =
        new Article("old title", "old description", "old body", Arrays.asList("java"), "author");
    UpdateArticleParam param = new UpdateArticleParam("new title", "new body", "new description");

    Article updated = articleCommandService.updateArticle(article, param);

    verify(articleRepository).save(article);
    assertThat(updated, is(article));
    assertThat(updated.getTitle(), is("new title"));
    assertThat(updated.getSlug(), is("new-title"));
    assertThat(updated.getDescription(), is("new description"));
    assertThat(updated.getBody(), is("new body"));
  }
}
