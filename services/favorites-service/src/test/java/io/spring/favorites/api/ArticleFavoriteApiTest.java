package io.spring.favorites.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.favorites.JacksonCustomizations;
import io.spring.favorites.api.security.WebSecurityConfig;
import io.spring.favorites.application.FavoritesQueryService;
import io.spring.favorites.core.favorite.ArticleFavorite;
import io.spring.favorites.core.favorite.ArticleFavoriteRepository;
import io.spring.favorites.infrastructure.monolith.dto.ArticleDto;
import io.spring.favorites.infrastructure.monolith.dto.ProfileDto;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleFavoriteApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class, FavoritesQueryService.class})
public class ArticleFavoriteApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  @MockBean private ArticleFavoritesReadService articleFavoritesReadService;

  private ArticleDto article;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    ProfileDto anotherUser = new ProfileDto(UUID.randomUUID().toString(), "other", "", "", false);
    DateTime now = new DateTime();
    article =
        new ArticleDto(
            UUID.randomUUID().toString(),
            "title",
            "title",
            "desc",
            "body",
            now,
            now,
            Arrays.asList("java"),
            anotherUser);
    when(monolithClient.findArticleBySlug(eq(article.getSlug()), eq(user.getId())))
        .thenReturn(article);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq(article.getId())))
        .thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(eq(article.getId()))).thenReturn(1);
  }

  @Test
  public void should_favorite_an_article_success() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()))
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1))
        .body("article.author.username", equalTo("other"))
        .body("article.author.id", equalTo(null));

    verify(articleFavoriteRepository).save(any());
  }

  @Test
  public void should_unfavorite_an_article_success() throws Exception {
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(new ArticleFavorite(article.getId(), user.getId())));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()));
    verify(articleFavoriteRepository).remove(new ArticleFavorite(article.getId(), user.getId()));
  }

  @Test
  public void should_401_without_a_token() throws Exception {
    given().when().post("/articles/{slug}/favorite", article.getSlug()).then().statusCode(401);
  }

  @Test
  public void should_404_when_the_monolith_does_not_know_the_slug() throws Exception {
    when(monolithClient.findArticleBySlug(eq("unknown"), eq(user.getId())))
        .thenThrow(new io.spring.favorites.api.exception.ResourceNotFoundException());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", "unknown")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_503_when_the_monolith_is_unavailable() throws Exception {
    when(monolithClient.findArticleBySlug(eq("flaky"), eq(user.getId())))
        .thenThrow(
            new io.spring.favorites.api.exception.MonolithUnavailableException(
                "GET /internal/articles/flaky", "read timed out"));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", "flaky")
        .then()
        .statusCode(503);
  }
}
