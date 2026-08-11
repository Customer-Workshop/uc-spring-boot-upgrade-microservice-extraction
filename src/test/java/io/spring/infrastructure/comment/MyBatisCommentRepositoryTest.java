package io.spring.infrastructure.comment;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisCommentRepository.class})
public class MyBatisCommentRepositoryTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;

  @Test
  public void should_create_and_fetch_comment_success() {
    Comment comment = new Comment("content", "123", "456");
    commentRepository.save(comment);

    Optional<Comment> optional = commentRepository.findById("456", comment.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), comment);
  }

  @Test
  public void should_remove_comment_success() {
    Comment comment = new Comment("content", "123", "456");
    commentRepository.save(comment);

    commentRepository.remove(comment);

    Assertions.assertFalse(commentRepository.findById("456", comment.getId()).isPresent());
  }

  @Test
  public void should_round_trip_comment_body_with_special_characters() {
    String body = "Nice 🎉 — it's \"100%\" <b>correct</b>;\nDROP TABLE comments;--\tdone \\o/";
    Comment comment = new Comment(body, "123", "456");
    commentRepository.save(comment);

    Optional<Comment> optional = commentRepository.findById("456", comment.getId());

    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(body, optional.get().getBody());
  }

  @Test
  public void should_return_empty_for_unknown_comment() {
    Assertions.assertFalse(commentRepository.findById("456", "not-exist").isPresent());
  }
}
