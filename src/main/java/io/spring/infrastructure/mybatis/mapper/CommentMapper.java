package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Maps comment persistence operations to SQL statements. */
@Mapper
public interface CommentMapper {
  /** Inserts a comment row. */
  void insert(@Param("comment") Comment comment);

  /** Finds a comment for an article and comment identifier. */
  Comment findById(@Param("articleId") String articleId, @Param("id") String id);

  /** Deletes a comment row by identifier. */
  void delete(@Param("id") String id);
}
