package io.spring.article.infrastructure.repository;

import io.spring.article.core.comment.Comment;
import io.spring.article.core.comment.CommentRepository;
import io.spring.article.infrastructure.mybatis.mapper.CommentMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCommentRepository implements CommentRepository {
  private CommentMapper commentMapper;

  public MyBatisCommentRepository(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  @Override
  public void save(Comment comment) {
    commentMapper.insert(comment);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return Optional.ofNullable(commentMapper.findById(articleId, id));
  }

  @Override
  public void remove(Comment comment) {
    commentMapper.delete(comment.getId());
  }
}
