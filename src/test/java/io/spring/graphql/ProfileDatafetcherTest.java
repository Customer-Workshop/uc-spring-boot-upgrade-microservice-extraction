package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image.png");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image.png", false);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
  }

  @Test
  void should_get_article_author() {
    ProfileData authorProfile = new ProfileData("uid", "author", "bio", "img", true);
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "art-id",
            "test-slug",
            "Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            java.util.Collections.emptyList(),
            authorProfile);

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername("author", user)).thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("author", result.getUsername());
    assertTrue(result.getFollowing());
  }

  @Test
  void should_get_comment_author() {
    ProfileData commentAuthorProfile = new ProfileData("uid", "commenter", "bio", "img", false);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("cid", "body", "art-id", now, now, commentAuthorProfile);

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Map<String, CommentData> map = new HashMap<>();
    map.put("cid", commentData);
    when(dfe.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("cid").build();
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername("commenter", user))
        .thenReturn(Optional.of(commentAuthorProfile));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commenter", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    ProfileData profileData = new ProfileData("uid", "someone", "bio", "img", true);
    when(profileQueryService.findByUsername("someone", user)).thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("someone");

    ProfilePayload result = profileDatafetcher.queryProfile("someone", dfe);

    assertNotNull(result);
    assertEquals("someone", result.getProfile().getUsername());
  }

  @Test
  void should_throw_not_found_for_unknown_profile() {
    when(profileQueryService.findByUsername("nobody", user)).thenReturn(Optional.empty());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("nobody");

    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("nobody", dfe));
  }
}
