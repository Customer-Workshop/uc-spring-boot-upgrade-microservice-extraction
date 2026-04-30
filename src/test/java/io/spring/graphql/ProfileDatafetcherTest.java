package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    when(dfe.getLocalContext()).thenReturn(user);
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
  }

  @Test
  void should_get_article_author() {
    Article article = Article.newBuilder().slug("test-slug").build();
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    ArticleData articleData =
        new ArticleData(
            "id", "test-slug", "title", "desc", "body", false, 0,
            new DateTime(), new DateTime(), Collections.emptyList(), profileData);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    Comment comment = Comment.newBuilder().id("commentId").build();
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData("commentId", "body", "articleId", new DateTime(), new DateTime(), profileData);

    Map<String, CommentData> map = new HashMap<>();
    map.put("commentId", commentData);

    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));
    when(dfe.getArgument("username")).thenReturn("testuser");

    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }
}
