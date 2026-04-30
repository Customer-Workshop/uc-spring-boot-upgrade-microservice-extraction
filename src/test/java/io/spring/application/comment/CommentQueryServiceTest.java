package io.spring.application.comment;

import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentDto;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({MyBatisUserRepository.class, CommentQueryService.class, MyBatisArticleRepository.class})
public class CommentQueryServiceTest extends DbTestBase {

  @MockBean private CommentServiceClient commentServiceClient;

  @Autowired private UserRepository userRepository;

  @Autowired private CommentQueryService commentQueryService;

  @Autowired private ArticleRepository articleRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_read_comment_success() {
    CommentDto dto = new CommentDto();
    dto.setId("comment-1");
    dto.setBody("content");
    dto.setUserId(user.getId());
    dto.setArticleId("123");

    when(commentServiceClient.getCommentById("comment-1")).thenReturn(Optional.of(dto));

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
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

    CommentDto dto1 = new CommentDto();
    dto1.setId("comment-1");
    dto1.setBody("content1");
    dto1.setUserId(user.getId());
    dto1.setArticleId(article.getId());

    CommentDto dto2 = new CommentDto();
    dto2.setId("comment-2");
    dto2.setBody("content2");
    dto2.setUserId(user2.getId());
    dto2.setArticleId(article.getId());

    when(commentServiceClient.getCommentsByArticleId(article.getId()))
        .thenReturn(Arrays.asList(dto1, dto2));

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
