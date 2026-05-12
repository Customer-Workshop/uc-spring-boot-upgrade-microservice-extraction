package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_user_profile() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  public void should_get_article_author_profile() {
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Desc",
            "Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            java.util.Collections.emptyList(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_get_comment_author_profile() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    Comment comment = Comment.newBuilder().id("comment-id").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  public void should_query_profile_by_username() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("testuser");
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  public void should_throw_not_found_when_profile_does_not_exist() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.getUserProfile(dataFetchingEnvironment));
  }
}
