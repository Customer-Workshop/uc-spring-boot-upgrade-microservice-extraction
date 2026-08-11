package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    Assertions.assertEquals(comments.size(), 2);
  }

  @Test
  public void should_return_empty_for_unknown_comment() {
    Assertions.assertFalse(commentQueryService.findById("not-exist", user).isPresent());
  }

  @Test
  public void should_return_empty_comments_for_unknown_article() {
    Assertions.assertTrue(commentQueryService.findByArticleId("not-exist", user).isEmpty());
  }

  @Test
  public void should_read_first_page_of_comments_with_cursor() {
    Article article = articleWithTwoComments();

    CursorPager<CommentData> firstPage =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 1, Direction.NEXT));

    Assertions.assertEquals(1, firstPage.getData().size());
    Assertions.assertTrue(firstPage.hasNext());
    Assertions.assertNotNull(firstPage.getStartCursor());
  }

  @Test
  public void should_read_previous_page_of_comments_with_cursor() {
    Article article = articleWithTwoComments();

    CursorPager<CommentData> previousPage =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 20, Direction.PREV));

    Assertions.assertEquals(2, previousPage.getData().size());
    Assertions.assertFalse(previousPage.hasPrevious());
  }

  @Test
  public void should_read_comments_with_cursor_for_anonymous_user() {
    Article article = articleWithTwoComments();

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), null, new CursorPageParameter<>(null, 20, Direction.NEXT));

    Assertions.assertEquals(2, page.getData().size());
    Assertions.assertFalse(page.getData().get(0).getProfileData().isFollowing());
  }

  private Article articleWithTwoComments() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    User user2 = new User("user2@email.com", "user2", "123", "", "");
    userRepository.save(user2);
    userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));
    commentRepository.save(new Comment("content1", user.getId(), article.getId()));
    commentRepository.save(new Comment("content2", user2.getId(), article.getId()));
    return article;
  }

  @Test
  public void should_return_empty_cursor_page_for_unknown_article() {
    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            "not-exist", user, new CursorPageParameter<>(null, 20, Direction.NEXT));

    Assertions.assertTrue(page.getData().isEmpty());
    Assertions.assertNull(page.getStartCursor());
  }
}
