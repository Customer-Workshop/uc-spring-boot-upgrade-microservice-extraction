package io.spring.articleservice;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.core.article.Article;
import java.util.ArrayList;
import java.util.Arrays;
import org.joda.time.DateTime;

public class TestHelper {
  public static ArticleData articleDataFixture(String seed, String userId) {
    DateTime now = new DateTime();
    return new ArticleData(
        seed + "id",
        "title-" + seed,
        "title " + seed,
        "desc " + seed,
        "body " + seed,
        false,
        0,
        now,
        now,
        new ArrayList<>(),
        new ProfileData(userId, userId, "", "", false));
  }

  public static ArticleData getArticleDataFromArticle(Article article, String userId) {
    return new ArticleData(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        false,
        0,
        article.getCreatedAt(),
        article.getUpdatedAt(),
        Arrays.asList("joda"),
        new ProfileData(userId, userId, "", "", false));
  }
}
