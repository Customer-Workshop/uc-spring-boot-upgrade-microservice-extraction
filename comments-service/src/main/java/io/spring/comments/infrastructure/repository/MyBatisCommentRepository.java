package io.spring.comments.infrastructure.repository;

import io.spring.comments.core.comment.Comment;
import io.spring.comments.core.comment.CommentRepository;
import io.spring.comments.infrastructure.mybatis.mapper.CommentMapper;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyBatisCommentRepository implements CommentRepository {
  private final CommentMapper commentMapper;

  @Autowired
  public MyBatisCommentRepository(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  @Override
  public void save(Comment comment) {
    commentMapper.insert(comment);
  }

  @Override
  public Optional<Comment> findById(String id) {
    return Optional.ofNullable(commentMapper.findById(id));
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findByArticleIdAndId(articleId, id));
  }

  @Override
  public List<Comment> findByArticleId(String articleId) {
    return commentMapper.findByArticleId(articleId);
  }

  @Override
  public List<Comment> findByArticleIdWithCursor(
      String articleId, DateTime cursor, int limit, boolean next) {
    return commentMapper.findByArticleIdWithCursor(articleId, cursor, limit, next);
  }

  @Override
  public void remove(Comment comment) {
    commentMapper.delete(comment.getId());
  }
}
