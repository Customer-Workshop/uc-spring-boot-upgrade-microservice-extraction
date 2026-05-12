package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_user_profile() {
    doReturn(user).when(dfe).getLocalContext();

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  public void should_throw_not_found_when_profile_not_found() {
    doReturn(user).when(dfe).getLocalContext();
    when(profileQueryService.findByUsername(eq("testuser"), eq(user))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> profileDatafetcher.getUserProfile(dfe));
  }

  @Test
  public void should_get_article_author() {
    Article article = Article.newBuilder().slug("test-slug").build();
    ProfileData authorProfile = new ProfileData("authorId", "author", "bio", "image", false);
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

    doReturn(map).when(dfe).getLocalContext();
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("author"), eq(user)))
        .thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("author", result.getUsername());
  }

  @Test
  public void should_get_comment_author() {
    Comment comment = Comment.newBuilder().id("commentId").build();
    ProfileData commentAuthorProfile =
        new ProfileData("authorId", "commentAuthor", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "commentId", "body", "articleId", new DateTime(), new DateTime(), commentAuthorProfile);

    Map<String, CommentData> map = new HashMap<>();
    map.put("commentId", commentData);

    doReturn(map).when(dfe).getLocalContext();
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("commentAuthor"), eq(user)))
        .thenReturn(Optional.of(commentAuthorProfile));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commentAuthor", result.getUsername());
  }

  @Test
  public void should_query_profile_by_username() {
    ProfileData profileData = new ProfileData("userId", "targetuser", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));
    when(dfe.getArgument("username")).thenReturn("targetuser");

    ProfilePayload result = profileDatafetcher.queryProfile("targetuser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }
}
