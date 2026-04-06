package io.spring.comments.repository;

import io.spring.comments.model.Comment;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CommentRepository {

  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<Comment> COMMENT_ROW_MAPPER =
      (ResultSet rs, int rowNum) -> {
        Comment comment = new Comment();
        comment.setId(rs.getString("id"));
        comment.setBody(rs.getString("body"));
        comment.setArticleId(rs.getString("article_id"));
        comment.setUserId(rs.getString("user_id"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
          comment.setCreatedAt(createdTs.toInstant());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
          comment.setUpdatedAt(updatedTs.toInstant());
        }
        return comment;
      };

  public Comment save(Comment comment) {
    jdbcTemplate.update(
        "INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getUserId(),
        Timestamp.from(comment.getCreatedAt()),
        Timestamp.from(comment.getUpdatedAt()));
    return comment;
  }

  public List<Comment> findByArticleIdOrderByCreatedAtDesc(String articleId) {
    return jdbcTemplate.query(
        "SELECT * FROM comments WHERE article_id = ? ORDER BY created_at DESC",
        COMMENT_ROW_MAPPER,
        articleId);
  }

  public Optional<Comment> findById(String id) {
    try {
      Comment comment =
          jdbcTemplate.queryForObject(
              "SELECT * FROM comments WHERE id = ?", COMMENT_ROW_MAPPER, id);
      return Optional.ofNullable(comment);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<Comment> findByIdAndArticleId(String id, String articleId) {
    try {
      Comment comment =
          jdbcTemplate.queryForObject(
              "SELECT * FROM comments WHERE id = ? AND article_id = ?",
              COMMENT_ROW_MAPPER,
              id,
              articleId);
      return Optional.ofNullable(comment);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public void deleteById(String id) {
    jdbcTemplate.update("DELETE FROM comments WHERE id = ?", id);
  }
}
