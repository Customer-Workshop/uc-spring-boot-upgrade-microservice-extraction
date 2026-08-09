package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ArticleCommandServiceTest {
  private final ArticleRepository repository = mock(ArticleRepository.class);
  private final ArticleCommandService service = new ArticleCommandService(repository);
  private final User user = new User("user@test.com", "user", "password", "", "");

  @Test
  void createsArticleAndPersistsIt() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Description")
            .body("Body")
            .tagList(Arrays.asList("java"))
            .build();
    Article result = service.createArticle(param, user);
    assertEquals("title", result.getSlug());
    assertEquals(user.getId(), result.getUserId());
    verify(repository).save(result);
  }

  @Test
  void updatesArticleAndPersistsIt() {
    Article article =
        new Article("old", "old description", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("new", "new description", "new body");
    assertEquals(article, service.updateArticle(article, param));
    assertEquals("new", article.getTitle());
    verify(repository).save(any(Article.class));
  }
}
