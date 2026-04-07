package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisCommentRepository.class,
  MyBatisUserRepository.class,
  CommentQueryService.class,
  MyBatisArticleRepository.class
})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CommentQueryService commentQueryService;

  @Autowired private ArticleRepository articleRepository;

  @MockBean private CommentServiceClient commentServiceClient;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_read_comment_success() {
    Comment comment = new Comment("content", user.getId(), "123");
    commentRepository.save(comment);

    CommentResponse response = new CommentResponse();
    response.setId(comment.getId());
    response.setBody(comment.getBody());
    response.setUserId(user.getId());
    response.setArticleId("123");
    response.setCreatedAt(comment.getCreatedAt().getMillis());
    response.setUpdatedAt(comment.getCreatedAt().getMillis());
    Mockito.when(commentServiceClient.findById(comment.getId())).thenReturn(Optional.of(response));

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    User user2 = new User("user2@email.com", "user2", "123", "", "");
    userRepository.save(user2);
    userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));

    Comment comment1 = new Comment("content1", user.getId(), article.getId());
    commentRepository.save(comment1);
    Comment comment2 = new Comment("content2", user2.getId(), article.getId());
    commentRepository.save(comment2);

    CommentResponse r1 = new CommentResponse();
    r1.setId(comment1.getId());
    r1.setBody(comment1.getBody());
    r1.setUserId(user.getId());
    r1.setArticleId(article.getId());
    r1.setCreatedAt(comment1.getCreatedAt().getMillis());
    r1.setUpdatedAt(comment1.getCreatedAt().getMillis());

    CommentResponse r2 = new CommentResponse();
    r2.setId(comment2.getId());
    r2.setBody(comment2.getBody());
    r2.setUserId(user2.getId());
    r2.setArticleId(article.getId());
    r2.setCreatedAt(comment2.getCreatedAt().getMillis());
    r2.setUpdatedAt(comment2.getCreatedAt().getMillis());

    Mockito.when(commentServiceClient.findByArticleId(article.getId())).thenReturn(Arrays.asList(r1, r2));

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
