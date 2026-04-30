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
import java.util.List;
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
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  private ProfileDatafetcher fetcher;
  private User user;

  @BeforeEach
  void setUp() {
    fetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    when(dfe.getLocalContext()).thenReturn(user);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = fetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_author() {
    ArticleData articleData =
        new ArticleData(
            "1",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            List.of(),
            new ProfileData("userId", "authoruser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("slug").build();
    when(dfe.getSource()).thenReturn(article);

    ProfileData profileData = new ProfileData("userId", "authoruser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("authoruser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = fetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("authoruser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    CommentData commentData =
        new CommentData(
            "c1",
            "body",
            "article1",
            new DateTime(),
            new DateTime(),
            new ProfileData("userId", "commentauthor", "bio", "image", false));
    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);
    when(dfe.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("c1").build();
    when(dfe.getSource()).thenReturn(comment);

    ProfileData profileData = new ProfileData("userId", "commentauthor", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("commentauthor"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = fetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commentauthor", result.getUsername());
  }

  @Test
  void should_query_profile() {
    when(dfe.getArgument("username")).thenReturn("someuser");
    ProfileData profileData = new ProfileData("userId", "someuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("someuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = fetcher.queryProfile("someuser", dfe);

    assertNotNull(result);
    assertEquals("someuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }

  @Test
  void should_throw_when_profile_not_found() {
    User unknownUser = new User("unknown@test.com", "missing", "pass", "", "");
    when(dfe.getLocalContext()).thenReturn(unknownUser);
    when(profileQueryService.findByUsername(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> fetcher.getUserProfile(dfe));
  }
}
