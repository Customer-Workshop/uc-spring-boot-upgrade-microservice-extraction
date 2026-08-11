package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest extends GraphQLTestBase {

  @Mock private ProfileQueryService profileQueryService;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    profileData = new ProfileData(user.getId(), user.getUsername(), "bio", "image", true);
  }

  @Test
  public void should_query_profile_of_local_context_user() {
    anonymous();
    when(dataFetchingEnvironment.<User>getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq(user.getUsername()), isNull()))
        .thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertEquals(user.getUsername(), profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image", profile.getImage());
    assertTrue(profile.getFollowing());
  }

  @Test
  public void should_query_profile_with_current_user_when_authenticated() {
    authenticate(user);
    when(dataFetchingEnvironment.getArgument(eq("username"))).thenReturn(user.getUsername());
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload =
        profileDatafetcher.queryProfile(user.getUsername(), dataFetchingEnvironment);

    assertEquals(user.getUsername(), payload.getProfile().getUsername());
  }

  @Test
  public void should_throw_not_found_for_unknown_username() {
    anonymous();
    when(dataFetchingEnvironment.getArgument(eq("username"))).thenReturn("ghost");
    when(profileQueryService.findByUsername(eq("ghost"), isNull())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.queryProfile("ghost", dataFetchingEnvironment));
  }

  @Test
  public void should_query_author_of_article() {
    anonymous();
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
    Article article = Article.newBuilder().slug("slug").build();
    when(dataFetchingEnvironment.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("slug", articleData));
    when(dataFetchingEnvironment.<Article>getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq(user.getUsername()), isNull()))
        .thenReturn(Optional.of(profileData));

    assertEquals(
        user.getUsername(), profileDatafetcher.getAuthor(dataFetchingEnvironment).getUsername());
  }

  @Test
  public void should_query_author_of_comment() {
    anonymous();
    CommentData commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    Comment comment = Comment.newBuilder().id("comment-id").build();
    when(dataFetchingEnvironment.<Map<String, CommentData>>getLocalContext())
        .thenReturn(Collections.singletonMap("comment-id", commentData));
    when(dataFetchingEnvironment.<Comment>getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq(user.getUsername()), isNull()))
        .thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertEquals(user.getUsername(), profile.getUsername());
    assertFalse(profile.getUsername().isEmpty());
  }
}
