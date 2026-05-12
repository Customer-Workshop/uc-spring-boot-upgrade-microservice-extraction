package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
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
import java.util.Arrays;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_user_profile_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername("testuser", null)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
  }

  @Test
  void should_throw_not_found_for_user_profile() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.getUserProfile(dataFetchingEnvironment));
  }

  @Test
  void should_get_article_author() {
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug", articleData);
    Article article = Article.newBuilder().slug("slug").build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("comment-id", "Nice", "article-id", now, now, profileData);
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    Comment comment = Comment.newBuilder().id("comment-id").body("Nice").build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_success() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("testuser");
    when(profileQueryService.findByUsername("testuser", user)).thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_not_found_when_query_nonexistent_profile() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
    when(profileQueryService.findByUsername("nonexistent", user)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment));
  }
}
