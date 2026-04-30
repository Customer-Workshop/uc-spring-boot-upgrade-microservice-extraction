package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileDatafetcherTest {

  private ProfileQueryService profileQueryService;
  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    profileQueryService = Mockito.mock(ProfileQueryService.class);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "123", "bio", "img");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_user_profile() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("img", result.getImage());
  }

  @Test
  public void should_get_article_author() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    ArticleData articleData =
        new ArticleData(
            "id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            new java.util.ArrayList<>(),
            profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_get_comment_author() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    Map<String, CommentData> map = new HashMap<>();
    map.put("cid", commentData);

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("cid").build();
    when(dfe.getSource()).thenReturn(comment);

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_query_profile_by_username() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }

  @Test
  public void should_throw_when_profile_not_found() {
    when(profileQueryService.findByUsername(eq("missing"), any())).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(new User("x@x.com", "missing", "123", "", ""));

    assertThrows(ResourceNotFoundException.class, () -> profileDatafetcher.getUserProfile(dfe));
  }
}
