package io.spring.comments.infrastructure;

import io.spring.comments.domain.Comment;
import io.spring.comments.domain.CommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MyBatisCommentRepository implements CommentRepository {
  private final CommentMapper commentMapper;

  @Override
  public void save(Comment comment) {
    commentMapper.insert(comment);
  }

  @Override
  public Optional<Comment> findById(String id) {
    return Optional.ofNullable(commentMapper.findById(id));
  }

  @Override
  public Optional<Comment> findByIdAndArticleId(String id, String articleId) {
    return Optional.ofNullable(commentMapper.findByIdAndArticleId(id, articleId));
  }

  @Override
  public List<Comment> findByArticleId(String articleId) {
    return commentMapper.findByArticleId(articleId);
  }

  @Override
  public void remove(String id) {
    commentMapper.delete(id);
  }
}
