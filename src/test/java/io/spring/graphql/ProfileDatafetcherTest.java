package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

class ProfileDatafetcherTest {

  private ProfileDatafetcher profileDatafetcher;
  private ProfileQueryService profileQueryService;
  private User user;

  @BeforeEach
  void setUp() {
    profileQueryService = mock(ProfileQueryService.class);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername("testuser", user))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void should_get_article_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    DateTime now = DateTime.now();
    ProfileData authorProfile = new ProfileData("authorId", "authoruser", "bio", "img", false);
    ArticleData articleData = new ArticleData(
        "art-id", "test-slug", "Title", "desc", "body", false, 0, now, now,
        Collections.emptyList(), authorProfile);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ProfileData profileData =
        new ProfileData("authorId", "authoruser", "bio", "img", true);
    when(profileQueryService.findByUsername("authoruser", user))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("authoruser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    DateTime now = DateTime.now();
    ProfileData commentProfile = new ProfileData("commenterId", "commenter", "bio", "img", false);
    CommentData commentData = new CommentData("c1", "body", "art-id", now, now, commentProfile);

    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    Comment comment = Comment.newBuilder().id("c1").build();
    when(dfe.getSource()).thenReturn(comment);

    ProfileData profileData =
        new ProfileData("commenterId", "commenter", "bio", "img", false);
    when(profileQueryService.findByUsername("commenter", user))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commenter", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("querieduser");

    ProfileData profileData =
        new ProfileData("id", "querieduser", "bio", "img", true);
    when(profileQueryService.findByUsername("querieduser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("querieduser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("querieduser", result.getProfile().getUsername());
  }
}
