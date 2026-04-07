package io.spring.comments.service;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {
  private CommentMapper commentMapper;

  public Comment createComment(String body, String userId, String articleId) {
    Comment comment = new Comment(body, userId, articleId);
    commentMapper.insert(comment);
    return comment;
  }

  public Optional<Comment> findById(String id) {
    return Optional.ofNullable(commentMapper.findById(id));
  }

  public Optional<Comment> findByArticleIdAndId(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findByArticleIdAndId(articleId, id));
  }

  public List<Comment> findByArticleId(String articleId) {
    return commentMapper.findByArticleId(articleId);
  }

  public void deleteComment(String id) {
    commentMapper.delete(id);
  }
}
