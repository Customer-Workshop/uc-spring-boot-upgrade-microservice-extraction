package io.spring.interaction.infrastructure.repository;

import io.spring.interaction.core.Comment;
import io.spring.interaction.core.CommentRepository;
import io.spring.interaction.infrastructure.mybatis.mapper.CommentMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisCommentRepository implements CommentRepository {
  private CommentMapper commentMapper;

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
