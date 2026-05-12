package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  void should_get_user_profile() {
    authenticateUser(user);
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void should_get_user_profile_as_anonymous() {
    setAnonymous();
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), isNull()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_article_author() {
    authenticateUser(user);
    ArticleData articleData = new ArticleData();
    articleData.setSlug("test-slug");
    articleData.setProfileData(profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    authenticateUser(user);
    ProfileData commentAuthorProfile =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "commentId", "body", "articleId", new DateTime(), new DateTime(), commentAuthorProfile);
    Map<String, CommentData> map = new HashMap<>();
    map.put("commentId", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    Comment comment = Comment.newBuilder().id("commentId").build();
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    authenticateUser(user);
    when(dfe.getArgument("username")).thenReturn("testuser");
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void should_return_profile_with_following_true() {
    authenticateUser(user);
    ProfileData followingProfile = new ProfileData(user.getId(), "testuser", "bio", "image", true);
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(followingProfile));

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertTrue(result.getFollowing());
  }
}
