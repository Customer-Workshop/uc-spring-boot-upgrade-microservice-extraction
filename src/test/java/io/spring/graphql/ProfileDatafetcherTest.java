package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
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
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @Test
  void should_get_user_profile() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);

      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
      when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
          .thenReturn(Optional.of(profileData));

      Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("testuser", result.getUsername());
      assertEquals("bio", result.getBio());
      assertEquals("image", result.getImage());
    }
  }

  @Test
  void should_get_article_author() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = Article.newBuilder().slug("test-slug").build();
      when(dataFetchingEnvironment.getSource()).thenReturn(article);

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
              null,
              profileData);
      Map<String, ArticleData> map = new HashMap<>();
      map.put("test-slug", articleData);
      when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
      when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
          .thenReturn(Optional.of(profileData));

      Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("testuser", result.getUsername());
    }
  }

  @Test
  void should_get_comment_author() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Comment comment = Comment.newBuilder().id("comment-id").build();
      when(dataFetchingEnvironment.getSource()).thenReturn(comment);

      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
      CommentData commentData =
          new CommentData(
              "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
      Map<String, CommentData> map = new HashMap<>();
      map.put("comment-id", commentData);
      when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
      when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
          .thenReturn(Optional.of(profileData));

      Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("testuser", result.getUsername());
    }
  }

  @Test
  void should_query_profile() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      when(dataFetchingEnvironment.getArgument("username")).thenReturn("testuser");
      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", true);
      when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
          .thenReturn(Optional.of(profileData));

      ProfilePayload result = profileDatafetcher.queryProfile("testuser", dataFetchingEnvironment);

      assertNotNull(result);
      assertNotNull(result.getProfile());
      assertEquals("testuser", result.getProfile().getUsername());
    }
  }

  @Test
  void should_throw_not_found_when_profile_not_found() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
      when(profileQueryService.findByUsername(eq("nonexistent"), eq(null)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment));
    }
  }
}
