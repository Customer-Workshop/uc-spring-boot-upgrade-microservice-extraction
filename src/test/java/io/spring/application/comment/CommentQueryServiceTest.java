package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {
  @Mock private CommentServiceClient commentServiceClient;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ProfileQueryService profileQueryService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService =
        new CommentQueryService(
            commentServiceClient, userRelationshipQueryService, profileQueryService);
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    CommentResponse cr = new CommentResponse();
    cr.setId("comment-1");
    cr.setBody("content");
    cr.setArticleId("123");
    cr.setUserId(user.getId());
    cr.setCreatedAt(new DateTime());
    cr.setUpdatedAt(new DateTime());

    when(commentServiceClient.getCommentById(eq("comment-1"))).thenReturn(Optional.of(cr));

    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUserId(eq(user.getId()))).thenReturn(Optional.of(profileData));

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(user.getUsername(), commentData.getProfileData().getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    String articleId = "article-1";
    String user2Id = "user-2";

    CommentResponse cr1 = new CommentResponse();
    cr1.setId("c1");
    cr1.setBody("content1");
    cr1.setArticleId(articleId);
    cr1.setUserId(user.getId());
    cr1.setCreatedAt(new DateTime());
    cr1.setUpdatedAt(new DateTime());

    CommentResponse cr2 = new CommentResponse();
    cr2.setId("c2");
    cr2.setBody("content2");
    cr2.setArticleId(articleId);
    cr2.setUserId(user2Id);
    cr2.setCreatedAt(new DateTime());
    cr2.setUpdatedAt(new DateTime());

    when(commentServiceClient.getCommentsByArticleId(eq(articleId)))
        .thenReturn(Arrays.asList(cr1, cr2));

    ProfileData profile1 = new ProfileData(user.getId(), "aisensiy", "", "", false);
    ProfileData profile2 = new ProfileData(user2Id, "user2", "", "", false);

    when(profileQueryService.findByUserId(eq(user.getId()))).thenReturn(Optional.of(profile1));
    when(profileQueryService.findByUserId(eq(user2Id))).thenReturn(Optional.of(profile2));
    when(userRelationshipQueryService.followingAuthors(
            eq(user.getId()), eq(Arrays.asList(user.getId(), user2Id))))
        .thenReturn(new HashSet<>(Collections.singletonList(user2Id)));

    List<CommentData> comments = commentQueryService.findByArticleId(articleId, user);
    Assertions.assertEquals(2, comments.size());
  }
}
