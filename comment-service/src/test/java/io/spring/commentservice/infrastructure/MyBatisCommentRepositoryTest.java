package io.spring.commentservice.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MyBatisCommentRepositoryTest {

  @Autowired private CommentRepository commentRepository;

  @Test
  void shouldSaveAndFindComment() {
    Comment comment = new Comment("test body", "user1", "article1");
    commentRepository.save(comment);

    Optional<Comment> found = commentRepository.findById(comment.getId());
    assertTrue(found.isPresent());
    assertEquals("test body", found.get().getBody());
    assertEquals("user1", found.get().getUserId());
    assertEquals("article1", found.get().getArticleId());
  }

  @Test
  void shouldFindByArticleId() {
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article1");
    Comment comment3 = new Comment("body3", "user1", "article2");
    commentRepository.save(comment1);
    commentRepository.save(comment2);
    commentRepository.save(comment3);

    List<Comment> comments = commentRepository.findByArticleId("article1");
    assertTrue(comments.size() >= 2);
    assertTrue(comments.stream().allMatch(c -> "article1".equals(c.getArticleId())));
  }

  @Test
  void shouldRemoveComment() {
    Comment comment = new Comment("test body", "user1", "article1");
    commentRepository.save(comment);

    commentRepository.remove(comment);
    Optional<Comment> found = commentRepository.findById(comment.getId());
    assertFalse(found.isPresent());
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    Optional<Comment> found = commentRepository.findById("nonexistent");
    assertFalse(found.isPresent());
  }
}
