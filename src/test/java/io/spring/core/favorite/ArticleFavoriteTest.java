package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_set_all_fields_in_constructor() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");
    assertThat(favorite.getArticleId(), is("article-id"));
    assertThat(favorite.getUserId(), is("user-id"));
  }

  @Test
  public void should_be_equal_when_article_and_user_are_same() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite same = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite different = new ArticleFavorite("article-id", "other-user");
    assertThat(favorite.equals(same), is(true));
    assertThat(favorite.hashCode(), is(same.hashCode()));
    assertThat(favorite.equals(different), is(false));
  }
}
