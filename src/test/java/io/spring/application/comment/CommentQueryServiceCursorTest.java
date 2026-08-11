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
import org.joda.time.DateTime;
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
public class CommentQueryServiceCursorTest extends DbTestBase {

  @Autowired private CommentRepository commentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CommentQueryService commentQueryService;

  @Autowired private ArticleRepository articleRepository;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("cursor@test.com", "cursoruser", "123", "", "");
    userRepository.save(user);
    article = new Article("cursor title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
  }

  @Test
  public void should_page_comments_forward_and_report_next_page() {
    for (int i = 0; i < 3; i++) {
      commentRepository.save(new Comment("content" + i, user.getId(), article.getId()));
    }

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 2, Direction.NEXT));

    Assertions.assertEquals(2, page.getData().size());
    Assertions.assertTrue(page.hasNext());
    Assertions.assertFalse(page.hasPrevious());
    Assertions.assertNotNull(page.getStartCursor());
  }

  @Test
  public void should_page_comments_backward_from_cursor() {
    Comment first = new Comment("first", user.getId(), article.getId());
    commentRepository.save(first);
    Comment second = new Comment("second", user.getId(), article.getId());
    commentRepository.save(second);

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(new DateTime(0L), 10, Direction.PREV));

    Assertions.assertEquals(2, page.getData().size());
    Assertions.assertFalse(page.hasPrevious());
  }

  @Test
  public void should_mark_followed_authors_in_cursor_page() {
    User author = new User("author@test.com", "cursorauthor", "123", "", "");
    userRepository.save(author);
    userRepository.saveRelation(new FollowRelation(user.getId(), author.getId()));
    commentRepository.save(new Comment("followed", author.getId(), article.getId()));

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    Assertions.assertTrue(page.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_page_for_article_without_comments() {
    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            "unknown-article", user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    Assertions.assertTrue(page.getData().isEmpty());
    Assertions.assertFalse(page.hasNext());
    Assertions.assertNull(page.getStartCursor());
  }

  @Test
  public void should_page_comments_for_anonymous_reader() {
    commentRepository.save(new Comment("anonymous readable", user.getId(), article.getId()));

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), null, new CursorPageParameter<>(null, 10, Direction.NEXT));

    Assertions.assertEquals(1, page.getData().size());
    Assertions.assertFalse(page.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_for_unknown_comment_id() {
    Optional<CommentData> optional = commentQueryService.findById("unknown-id", user);

    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_return_empty_list_for_unknown_article_id() {
    List<CommentData> comments = commentQueryService.findByArticleId("unknown-article", user);

    Assertions.assertTrue(comments.isEmpty());
  }

  @Test
  public void should_remove_comment_and_not_find_it_anymore() {
    Comment comment = new Comment("to be removed", user.getId(), article.getId());
    commentRepository.save(comment);

    commentRepository.remove(comment);

    Assertions.assertFalse(
        commentRepository.findById(article.getId(), comment.getId()).isPresent());
  }
}
