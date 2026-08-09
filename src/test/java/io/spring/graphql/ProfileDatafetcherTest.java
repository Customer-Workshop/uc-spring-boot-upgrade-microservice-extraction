package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.graphql.types.Profile;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ProfileDatafetcherTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final Article article = GraphqlTestFixtures.article(user);
  private final Comment comment = GraphqlTestFixtures.comment(article, user);
  private final ProfileData profile = GraphqlTestFixtures.profileData(user, true);

  @Test
  void getsUserProfileAndQueryProfile() {
    ProfileQueryService service = mock(ProfileQueryService.class);
    when(service.findByUsername(user.getUsername(), null)).thenReturn(Optional.of(profile));
    ProfileDatafetcher fetcher = new ProfileDatafetcher(service);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertEquals(
          user.getUsername(),
          fetcher.getUserProfile(GraphqlTestFixtures.environment(user)).getUsername());
      var environment = mock(graphql.schema.DataFetchingEnvironment.class);
      when(environment.getArgument("username")).thenReturn(user.getUsername());
      assertEquals(user.getUsername(), fetcher.queryProfile("ignored", environment).getProfile().getUsername());
    }
  }

  @Test
  void getsArticleAndCommentAuthors() {
    ProfileQueryService service = mock(ProfileQueryService.class);
    when(service.findByUsername(user.getUsername(), null)).thenReturn(Optional.of(profile));
    ProfileDatafetcher fetcher = new ProfileDatafetcher(service);
    ArticleData articleData = GraphqlTestFixtures.articleData(article, user);
    CommentData commentData = GraphqlTestFixtures.commentData(comment, article, user);
    Map<String, ArticleData> articles = GraphqlTestFixtures.articleContext(article, articleData);
    Map<String, CommentData> comments = java.util.Collections.singletonMap(comment.getId(), commentData);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      var articleEnvironment = mock(graphql.schema.DataFetchingEnvironment.class);
      when(articleEnvironment.getLocalContext()).thenReturn(articles);
      when(articleEnvironment.getSource()).thenReturn(io.spring.graphql.types.Article.newBuilder().slug(article.getSlug()).build());
      assertEquals(user.getUsername(), fetcher.getAuthor(articleEnvironment).getUsername());
      var commentEnvironment = mock(graphql.schema.DataFetchingEnvironment.class);
      when(commentEnvironment.getLocalContext()).thenReturn(comments);
      when(commentEnvironment.getSource()).thenReturn(io.spring.graphql.types.Comment.newBuilder().id(comment.getId()).build());
      assertEquals(user.getUsername(), fetcher.getCommentAuthor(commentEnvironment).getUsername());
    }
  }

  @Test
  void rejectsMissingProfile() {
    ProfileQueryService service = mock(ProfileQueryService.class);
    when(service.findByUsername(user.getUsername(), null)).thenReturn(Optional.empty());
    ProfileDatafetcher fetcher = new ProfileDatafetcher(service);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(
          ResourceNotFoundException.class,
          () -> fetcher.getUserProfile(GraphqlTestFixtures.environment(user)));
    }
  }
}
