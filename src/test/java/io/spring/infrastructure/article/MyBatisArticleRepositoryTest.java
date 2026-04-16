package io.spring.infrastructure.article;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import io.spring.infrastructure.service.TagServiceClient;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class MyBatisArticleRepositoryTest extends DbTestBase {
  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @MockBean private TagServiceClient tagServiceClient;

  private Article article;

  @BeforeEach
  public void setUp() {
    when(tagServiceClient.findOrCreateTag(anyString()))
        .thenAnswer(invocation -> UUID.randomUUID().toString());
    doNothing().when(tagServiceClient).createArticleTagRelation(anyString(), anyString());
    User user = new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
    userRepository.save(user);
    article = new Article("test", "desc", "body", Arrays.asList("java", "spring"), user.getId());
  }

  @Test
  public void should_create_and_fetch_article_success() {
    articleRepository.save(article);
    Optional<Article> optional = articleRepository.findById(article.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getId(), article.getId());
    Assertions.assertEquals(optional.get().getTitle(), article.getTitle());
    Assertions.assertEquals(optional.get().getBody(), article.getBody());
    // Tags are now managed by the Tags microservice, not stored locally
  }

  @Test
  public void should_update_and_fetch_article_success() {
    articleRepository.save(article);

    String newTitle = "new test 2";
    article.update(newTitle, "", "");
    articleRepository.save(article);
    System.out.println(article.getSlug());
    Optional<Article> optional = articleRepository.findBySlug(article.getSlug());
    Assertions.assertTrue(optional.isPresent());
    Article fetched = optional.get();
    Assertions.assertEquals(fetched.getTitle(), newTitle);
    Assertions.assertNotEquals(fetched.getBody(), "");
  }

  @Test
  public void should_delete_article() {
    articleRepository.save(article);

    articleRepository.remove(article);
    Assertions.assertFalse(articleRepository.findById(article.getId()).isPresent());
  }
}
