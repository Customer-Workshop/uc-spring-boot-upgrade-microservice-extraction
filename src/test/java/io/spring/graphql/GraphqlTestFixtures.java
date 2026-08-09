package io.spring.graphql;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.mockito.Mockito;

final class GraphqlTestFixtures {
  private GraphqlTestFixtures() {}

  static User user() {
    return new User("user@test.com", "user", "password", "bio", "image");
  }

  static Article article(User user) {
    return new Article("title", "description", "body", Arrays.asList("java"), user.getId());
  }

  static ArticleData articleData(Article article, User user) {
    return new ArticleData(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        false,
        1,
        new DateTime(),
        new DateTime(),
        Arrays.asList("java"),
        profileData(user, false));
  }

  static Comment comment(Article article, User user) {
    return new Comment("body", user.getId(), article.getId());
  }

  static CommentData commentData(Comment comment, Article article, User user) {
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        article.getId(),
        new DateTime(),
        new DateTime(),
        profileData(user, false));
  }

  static ProfileData profileData(User user, boolean following) {
    return new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), following);
  }

  static DataFetchingEnvironment environment(Object localContext) {
    DataFetchingEnvironment environment = Mockito.mock(DataFetchingEnvironment.class);
    Mockito.when(environment.getLocalContext()).thenReturn(localContext);
    return environment;
  }

  static DgsDataFetchingEnvironment dgsEnvironment(Object source, Object localContext) {
    DgsDataFetchingEnvironment environment = Mockito.mock(DgsDataFetchingEnvironment.class);
    Mockito.when(environment.getSource()).thenReturn(source);
    Mockito.when(environment.getLocalContext()).thenReturn(localContext);
    return environment;
  }

  static Map<String, ArticleData> articleContext(Article article, ArticleData data) {
    Map<String, ArticleData> context = new HashMap<>();
    context.put(article.getSlug(), data);
    return context;
  }
}
