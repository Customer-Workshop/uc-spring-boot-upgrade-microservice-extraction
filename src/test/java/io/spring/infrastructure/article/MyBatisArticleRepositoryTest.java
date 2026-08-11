package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class MyBatisArticleRepositoryTest extends DbTestBase {
  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  private Article article;

  @BeforeEach
  public void setUp() {
    User user = new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
    userRepository.save(user);
    article = new Article("test", "desc", "body", Arrays.asList("java", "spring"), user.getId());
  }

  @Test
  public void should_create_and_fetch_article_success() {
    articleRepository.save(article);
    Optional<Article> optional = articleRepository.findById(article.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), article);
    Assertions.assertTrue(optional.get().getTags().contains(new Tag("java")));
    Assertions.assertTrue(optional.get().getTags().contains(new Tag("spring")));
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
  public void should_round_trip_article_with_special_characters() {
    Article specialArticle =
        new Article(
            "Robert'); DROP TABLE articles;-- 日本語 🚀",
            "100% \"quoted\" & <b>bold</b>",
            "line1\nline2\ttabbed \\ backslash",
            Arrays.asList("c++", "c#", ".net"),
            article.getUserId());
    articleRepository.save(specialArticle);

    Optional<Article> optional = articleRepository.findById(specialArticle.getId());

    Assertions.assertTrue(optional.isPresent());
    Article fetched = optional.get();
    Assertions.assertEquals("Robert'); DROP TABLE articles;-- 日本語 🚀", fetched.getTitle());
    Assertions.assertEquals("100% \"quoted\" & <b>bold</b>", fetched.getDescription());
    Assertions.assertEquals("line1\nline2\ttabbed \\ backslash", fetched.getBody());
    Assertions.assertTrue(fetched.getTags().contains(new Tag("c++")));
    Assertions.assertTrue(fetched.getTags().contains(new Tag(".net")));
  }

  @Test
  public void should_fetch_article_by_slug_with_special_characters() {
    Article specialArticle =
        new Article(
            "Ünïcödé & emoji 🎉 title", "desc", "body", Arrays.asList("java"), article.getUserId());
    articleRepository.save(specialArticle);

    Optional<Article> optional = articleRepository.findBySlug(specialArticle.getSlug());

    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(specialArticle.getId(), optional.get().getId());
  }

  @Test
  public void should_not_match_slug_with_sql_wildcards() {
    articleRepository.save(article);

    Assertions.assertFalse(articleRepository.findBySlug("%").isPresent());
    Assertions.assertFalse(articleRepository.findBySlug("' OR '1'='1").isPresent());
  }

  @Test
  public void should_delete_article() {
    articleRepository.save(article);

    articleRepository.remove(article);
    Assertions.assertFalse(articleRepository.findById(article.getId()).isPresent());
  }
}
