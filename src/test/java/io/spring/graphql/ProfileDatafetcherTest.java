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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileDatafetcherTest {

  private ProfileQueryService profileQueryService;
  private ProfileDatafetcher profileDatafetcher;
  private User user;
  private DataFetchingEnvironment dfe;

  @BeforeEach
  public void setUp() {
    profileQueryService = mock(ProfileQueryService.class);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    dfe = mock(DataFetchingEnvironment.class);

    user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_user_profile() {
    User coreUser = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    when(dfe.getLocalContext()).thenReturn(coreUser);

    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image.jpg", result.getImage());
  }

  @Test
  public void should_get_article_author() {
    ProfileData authorProfile =
        new ProfileData("author-id", "author", "bio", "img", false);
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Title",
            "Desc",
            "Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            authorProfile);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    Article gqlArticle = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(gqlArticle);

    when(profileQueryService.findByUsername(eq("author"), any()))
        .thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("author", result.getUsername());
  }

  @Test
  public void should_get_comment_author() {
    ProfileData commentAuthor =
        new ProfileData("comment-author-id", "commentauthor", "bio", "img", false);
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), commentAuthor);

    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    Comment gqlComment = Comment.newBuilder().id("comment-id").build();
    when(dfe.getSource()).thenReturn(gqlComment);

    when(profileQueryService.findByUsername(eq("commentauthor"), any()))
        .thenReturn(Optional.of(commentAuthor));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("commentauthor", result.getUsername());
  }

  @Test
  public void should_query_profile_by_username() {
    when(dfe.getArgument("username")).thenReturn("testuser");
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image.jpg", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }

  @Test
  public void should_throw_not_found_when_profile_not_exist() {
    User coreUser = new User("nonexistent@test.com", "nonexistent", "pass", "", "");
    when(dfe.getLocalContext()).thenReturn(coreUser);
    when(profileQueryService.findByUsername(eq("nonexistent"), any()))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.getUserProfile(dfe));
  }
}
