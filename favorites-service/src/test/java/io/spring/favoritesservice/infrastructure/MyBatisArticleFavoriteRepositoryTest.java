package io.spring.favoritesservice.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.favoritesservice.core.ArticleFavorite;
import io.spring.favoritesservice.core.ArticleFavoriteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MyBatisArticleFavoriteRepositoryTest {

  @Autowired private ArticleFavoriteRepository repository;

  @Test
  void shouldSaveAndFind() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    repository.save(favorite);

    Optional<ArticleFavorite> found = repository.find("article1", "user1");
    assertTrue(found.isPresent());
    assertEquals("article1", found.get().getArticleId());
    assertEquals("user1", found.get().getUserId());
  }

  @Test
  void shouldNotDuplicateOnSave() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    repository.save(favorite);
    repository.save(favorite);

    int count = repository.countByArticleId("article1");
    assertEquals(1, count);
  }

  @Test
  void shouldRemove() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    repository.save(favorite);

    repository.remove(favorite);
    Optional<ArticleFavorite> found = repository.find("article1", "user1");
    assertFalse(found.isPresent());
  }

  @Test
  void shouldCountByArticleId() {
    repository.save(new ArticleFavorite("article1", "user1"));
    repository.save(new ArticleFavorite("article1", "user2"));
    repository.save(new ArticleFavorite("article2", "user1"));

    assertEquals(2, repository.countByArticleId("article1"));
    assertEquals(1, repository.countByArticleId("article2"));
  }

  @Test
  void shouldCheckIsUserFavorite() {
    repository.save(new ArticleFavorite("article1", "user1"));

    assertTrue(repository.isUserFavorite("article1", "user1"));
    assertFalse(repository.isUserFavorite("article1", "user2"));
  }
}
