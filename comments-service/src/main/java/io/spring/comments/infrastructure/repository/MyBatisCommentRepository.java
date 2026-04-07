package io.spring.comments.infrastructure.repository;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import io.spring.comments.infrastructure.mybatis.mapper.CommentMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyBatisCommentRepository implements CommentRepository {
  private CommentMapper commentMapper;

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
  public Optional<Comment> findByArticleIdAndId(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findByArticleIdAndId(articleId, id));
  }

  @Override
  public List<Comment> findByArticleId(String articleId) {
    return commentMapper.findByArticleId(articleId);
  }

  @Override
  public void remove(Comment comment) {
    commentMapper.delete(comment.getId());
  }
}
