package io.spring.article.service;

import io.spring.article.domain.Comment;
import io.spring.article.repository.CommentMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommentService {
  private CommentMapper commentMapper;

  @Transactional
  public Comment createComment(String body, String userId, String articleId) {
    Comment comment = new Comment(body, userId, articleId);
    commentMapper.insert(comment);
    return comment;
  }

  public Optional<Comment> findById(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findById(articleId, id));
  }

  public List<Comment> findByArticleId(String articleId) {
    return commentMapper.findByArticleId(articleId);
  }

  @Transactional
  public boolean deleteComment(String articleId, String id) {
    Comment comment = commentMapper.findById(articleId, id);
    if (comment == null) {
      return false;
    }
    commentMapper.delete(id);
    return true;
  }
}
