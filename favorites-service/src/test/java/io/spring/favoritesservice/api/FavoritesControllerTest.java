package io.spring.favoritesservice.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.spring.favoritesservice.application.dto.ArticleDto;
import io.spring.favoritesservice.core.ArticleFavorite;
import io.spring.favoritesservice.core.ArticleFavoriteRepository;
import io.spring.favoritesservice.infrastructure.client.MonolithClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FavoritesController.class)
public class FavoritesControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private MonolithClient monolithClient;

  @Test
  void shouldFavoriteArticle() throws Exception {
    ArticleDto article = new ArticleDto("article-id", "test-slug", "Test Title");
    when(monolithClient.getArticleBySlug("test-slug")).thenReturn(article);
    when(articleFavoriteRepository.isUserFavorite("article-id", "user1")).thenReturn(true);
    when(articleFavoriteRepository.countByArticleId("article-id")).thenReturn(1);

    mockMvc
        .perform(post("/articles/test-slug/favorite").header("X-User-Id", "user1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favorited").value(true))
        .andExpect(jsonPath("$.favoritesCount").value(1));
  }

  @Test
  void shouldUnfavoriteArticle() throws Exception {
    ArticleDto article = new ArticleDto("article-id", "test-slug", "Test Title");
    when(monolithClient.getArticleBySlug("test-slug")).thenReturn(article);
    when(articleFavoriteRepository.find("article-id", "user1"))
        .thenReturn(Optional.of(new ArticleFavorite("article-id", "user1")));
    when(articleFavoriteRepository.isUserFavorite("article-id", "user1")).thenReturn(false);
    when(articleFavoriteRepository.countByArticleId("article-id")).thenReturn(0);

    mockMvc
        .perform(delete("/articles/test-slug/favorite").header("X-User-Id", "user1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favorited").value(false));
  }

  @Test
  void shouldGetFavoriteCount() throws Exception {
    when(articleFavoriteRepository.countByArticleId("article-id")).thenReturn(5);

    mockMvc
        .perform(get("/articles/article-id/favorites/count"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(5));
  }

  @Test
  void shouldGetFavoriteStatus() throws Exception {
    when(articleFavoriteRepository.isUserFavorite("article-id", "user1")).thenReturn(true);

    mockMvc
        .perform(get("/articles/article-id/favorites/status?userId=user1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favorited").value(true));
  }

  @Test
  void shouldReturn404WhenArticleNotFound() throws Exception {
    when(monolithClient.getArticleBySlug("nonexistent")).thenReturn(null);

    mockMvc
        .perform(post("/articles/nonexistent/favorite").header("X-User-Id", "user1"))
        .andExpect(status().isNotFound());
  }
}
