package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
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
import java.util.ArrayList;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;

  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_get_user_profile() {
    authenticateUser(user);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  public void should_get_article_author_profile() {
    authenticateUser(user);
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "articleId",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Article article = Article.newBuilder().slug("test-slug").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(article);

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_get_comment_author_profile() {
    authenticateUser(user);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "commentId",
            "body",
            "articleId",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, CommentData> map = new HashMap<>();
    map.put("commentId", commentData);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Comment comment = Comment.newBuilder().id("commentId").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(comment);

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_query_profile_by_username() {
    authenticateUser(user);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }

  @Test
  public void should_throw_not_found_when_profile_not_exists() {
    authenticateUser(user);
    when(profileQueryService.findByUsername(eq("nonexistent"), eq(user)))
        .thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("nonexistent");

    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("nonexistent", dfe));
  }

  @Test
  public void should_query_profile_without_auth() {
    setAnonymous();
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(null)))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertFalse(result.getProfile().getFollowing());
  }
}
