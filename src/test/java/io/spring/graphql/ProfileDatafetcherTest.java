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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void loginAs(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  void should_get_user_profile() {
    when(dfe.getLocalContext()).thenReturn(user);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));
    loginAs(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void should_get_article_author() {
    loginAs(user);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ProfileData authorProfile = new ProfileData("author-id", "authoruser", "bio", "image", false);
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
            null,
            authorProfile);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    when(profileQueryService.findByUsername("authoruser", user))
        .thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("authoruser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    loginAs(user);
    Comment comment = Comment.newBuilder().id("comment-id").build();
    when(dfe.getSource()).thenReturn(comment);

    ProfileData authorProfile = new ProfileData("author-id", "authoruser", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), authorProfile);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    when(profileQueryService.findByUsername("authoruser", user))
        .thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("authoruser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    loginAs(user);
    ProfileData profileData = new ProfileData("target-id", "targetuser", "bio", "image", true);
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));
    when(dfe.getArgument("username")).thenReturn("targetuser");

    ProfilePayload result = profileDatafetcher.queryProfile("targetuser", dfe);
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_not_found_for_nonexistent_profile() {
    loginAs(user);
    when(profileQueryService.findByUsername("nonexistent", user)).thenReturn(Optional.empty());
    when(dfe.getArgument("username")).thenReturn("nonexistent");

    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("nonexistent", dfe));
  }
}
