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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

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

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void should_get_user_profile() {
    setAuthenticated(user);
    doReturn(user).when(dataFetchingEnvironment).getLocalContext();
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
  }

  @Test
  void should_get_article_author() {
    setAnonymous();
    Article article = Article.newBuilder().slug("test-slug").build();
    doReturn(article).when(dataFetchingEnvironment).getSource();

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
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
            Collections.emptyList(),
            profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    doReturn(map).when(dataFetchingEnvironment).getLocalContext();
    when(profileQueryService.findByUsername("testuser", null)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    setAnonymous();
    Comment comment = Comment.newBuilder().id("comment-id").build();
    doReturn(comment).when(dataFetchingEnvironment).getSource();

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    doReturn(map).when(dataFetchingEnvironment).getLocalContext();
    when(profileQueryService.findByUsername("testuser", null)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile() {
    setAnonymous();
    doReturn("testuser").when(dataFetchingEnvironment).getArgument("username");
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername("testuser", null)).thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_not_found_when_profile_not_exists() {
    setAnonymous();
    doReturn("nonexistent").when(dataFetchingEnvironment).getArgument("username");
    when(profileQueryService.findByUsername("nonexistent", null)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment));
  }

  @Test
  void should_throw_not_found_when_user_profile_not_exists() {
    User unknownUser = new User("unknown@test.com", "unknown", "pass", "", "");
    setAuthenticated(unknownUser);
    doReturn(unknownUser).when(dataFetchingEnvironment).getLocalContext();
    when(profileQueryService.findByUsername("unknown", unknownUser)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.getUserProfile(dataFetchingEnvironment));
  }
}
