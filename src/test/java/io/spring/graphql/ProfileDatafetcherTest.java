package io.spring.graphql;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest extends GraphQLTestBase {

  @Mock private ProfileQueryService profileQueryService;

  private ProfileDatafetcher profileDatafetcher;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("a@test.com", "a", "123", "bio", "image");
    profileData = new ProfileData(user.getId(), user.getUsername(), "bio", "image", true);
  }

  @Test
  public void should_get_user_profile() {
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(null)))
        .thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<User>getLocalContext()).thenReturn(user);

    Profile profile = profileDatafetcher.getUserProfile(dfe);

    Assertions.assertEquals(user.getUsername(), profile.getUsername());
    Assertions.assertEquals("bio", profile.getBio());
    Assertions.assertTrue(profile.getFollowing());
  }

  @Test
  public void should_get_article_author() {
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            profileData);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(null)))
        .thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("slug", articleData));
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("slug").build());

    Assertions.assertEquals(user.getUsername(), profileDatafetcher.getAuthor(dfe).getUsername());
  }

  @Test
  public void should_get_comment_author() {
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(null)))
        .thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<Map<String, CommentData>>getLocalContext())
        .thenReturn(Collections.singletonMap("comment-id", commentData));
    when(dfe.<Comment>getSource()).thenReturn(Comment.newBuilder().id("comment-id").build());

    Assertions.assertEquals(
        user.getUsername(), profileDatafetcher.getCommentAuthor(dfe).getUsername());
  }

  @Test
  public void should_query_profile_with_current_user() {
    User currentUser = new User("b@test.com", "b", "123", "", "");
    setCurrentUser(currentUser);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(currentUser)))
        .thenReturn(Optional.of(profileData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<String>getArgument(eq("username"))).thenReturn(user.getUsername());

    ProfilePayload payload = profileDatafetcher.queryProfile(user.getUsername(), dfe);

    Assertions.assertEquals(user.getUsername(), payload.getProfile().getUsername());
  }

  @Test
  public void should_throw_not_found_for_unknown_profile() {
    when(profileQueryService.findByUsername(eq("unknown"), eq(null))).thenReturn(Optional.empty());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<String>getArgument(eq("username"))).thenReturn("unknown");

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("unknown", dfe));
  }
}
