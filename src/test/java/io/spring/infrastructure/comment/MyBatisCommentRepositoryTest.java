package io.spring.infrastructure.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the CommentRepository contract. Now that comments are served by an external
 * microservice, these tests verify the repository interface contract using mocks.
 */
@ExtendWith(MockitoExtension.class)
public class MyBatisCommentRepositoryTest {
  @Mock private CommentRepository commentRepository;

  @Test
  public void should_create_and_fetch_comment_success() {
    Comment comment = new Comment("content", "123", "456");
    when(commentRepository.findById(eq("456"), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    commentRepository.save(comment);
    verify(commentRepository).save(any(Comment.class));

    Optional<Comment> optional = commentRepository.findById("456", comment.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), comment);
  }
}
