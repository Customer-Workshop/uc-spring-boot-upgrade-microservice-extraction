package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceReadClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentServiceReadClient commentServiceReadClient;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    DateTime now = new DateTime();
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData =
        new CommentData("comment-id", "content", "123", now, now, profileData);

    when(commentServiceReadClient.findById(eq("comment-id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById("comment-id", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData result = optional.get();
    Assertions.assertEquals(result.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    DateTime now = new DateTime();
    ProfileData profileData1 =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    ProfileData profileData2 = new ProfileData("user2-id", "user2", "", "", false);
    CommentData commentData1 =
        new CommentData("comment-1", "content1", "article-id", now, now, profileData1);
    CommentData commentData2 =
        new CommentData("comment-2", "content2", "article-id", now, now, profileData2);

    when(commentServiceReadClient.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData1, commentData2));
    when(userRelationshipQueryService.followingAuthors(
            eq(user.getId()), eq(Arrays.asList(user.getId(), "user2-id"))))
        .thenReturn(new HashSet<>(Collections.singletonList("user2-id")));

    List<CommentData> comments = commentQueryService.findByArticleId("article-id", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
