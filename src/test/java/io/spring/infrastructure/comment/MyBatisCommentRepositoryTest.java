package io.spring.infrastructure.comment;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MyBatisCommentRepositoryTest {
  @Mock private CommentServiceClient commentServiceClient;

  @InjectMocks private MyBatisCommentRepository commentRepository;

  @Test
  public void should_create_and_fetch_comment_success() {
    Comment comment = new Comment("content", "123", "456");
    CommentResponse response =
        new CommentResponse(
            comment.getId(),
            comment.getBody(),
            comment.getUserId(),
            comment.getArticleId(),
            comment.getCreatedAt().toString(),
            comment.getCreatedAt().toString());

    when(commentServiceClient.createComment(
            eq(comment.getBody()), eq(comment.getUserId()), eq(comment.getArticleId())))
        .thenReturn(response);
    when(commentServiceClient.getCommentById(eq(comment.getId()), eq("456")))
        .thenReturn(Optional.of(response));

    commentRepository.save(comment);
    verify(commentServiceClient)
        .createComment(eq(comment.getBody()), eq(comment.getUserId()), eq(comment.getArticleId()));

    Optional<Comment> optional = commentRepository.findById("456", comment.getId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getBody(), comment.getBody());
  }
}
