package io.spring.favorites.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.favorites.JacksonCustomizations;
import io.spring.favorites.api.security.WebSecurityConfig;
import io.spring.favorites.application.FavoritesQueryService;
import io.spring.favorites.application.data.ArticleFavoriteCount;
import io.spring.favorites.core.service.JwtService;
import io.spring.favorites.infrastructure.monolith.MonolithClient;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalFavoritesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class, FavoritesQueryService.class})
public class InternalFavoritesApiTest {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleFavoritesReadService articleFavoritesReadService;

  @MockBean private MonolithClient monolithClient;

  @MockBean private JwtService jwtService;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_return_counts_and_viewer_state_without_a_token() {
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(
            Arrays.asList(
                new ArticleFavoriteCount("article-1", 2),
                new ArticleFavoriteCount("article-2", 1)));
    when(articleFavoritesReadService.userFavorites(any(), eq("user-1")))
        .thenReturn(new HashSet<>(Collections.singletonList("article-2")));

    given()
        .when()
        .get("/internal/favorites?articleIds=article-1,article-2,article-3&viewerId=user-1")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("favorites.size()", equalTo(3))
        .body("favorites[0].articleId", equalTo("article-1"))
        .body("favorites[0].favoritesCount", equalTo(2))
        .body("favorites[0].favorited", equalTo(false))
        .body("favorites[1].articleId", equalTo("article-2"))
        .body("favorites[1].favoritesCount", equalTo(1))
        .body("favorites[1].favorited", equalTo(true))
        .body("favorites[2].articleId", equalTo("article-3"))
        .body("favorites[2].favoritesCount", equalTo(0))
        .body("favorites[2].favorited", equalTo(false));
  }

  @Test
  public void should_not_query_viewer_favorites_without_a_viewer() {
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Collections.singletonList(new ArticleFavoriteCount("article-1", 2)));

    given()
        .when()
        .get("/internal/favorites?articleIds=article-1")
        .then()
        .statusCode(200)
        .body("favorites[0].favoritesCount", equalTo(2))
        .body("favorites[0].favorited", equalTo(false));

    org.mockito.Mockito.verify(articleFavoritesReadService, org.mockito.Mockito.never())
        .userFavorites(any(), anyString());
  }

  @Test
  public void should_return_an_empty_list_for_no_ids() {
    List<?> ignored = Collections.emptyList();
    given()
        .when()
        .get("/internal/favorites")
        .then()
        .statusCode(200)
        .body("favorites.size()", equalTo(ignored.size()));
  }
}
