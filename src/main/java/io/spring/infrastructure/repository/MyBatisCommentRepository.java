package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.mybatis.mapper.CommentMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Persists comments through the MyBatis mapper. */
@Component
public class MyBatisCommentRepository implements CommentRepository {
  private CommentMapper commentMapper;

  @Autowired
  public MyBatisCommentRepository(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  /** Inserts a comment row. */
  @Override
  public void save(Comment comment) {
    commentMapper.insert(comment);
  }

  /** Finds a comment for an article and comment identifier. */
  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findById(articleId, id));
  }

  /** Deletes the supplied comment by identifier. */
  @Override
  public void remove(Comment comment) {
    commentMapper.delete(comment.getId());
  }
}
