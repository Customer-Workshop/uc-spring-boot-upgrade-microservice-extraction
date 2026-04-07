package io.spring.comments.infrastructure;

import io.spring.comments.core.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  void insert(@Param("comment") Comment comment);

  Comment findById(@Param("articleId") String articleId, @Param("id") String id);

  java.util.List<Comment> findByArticleId(@Param("articleId") String articleId);

  Comment findByIdOnly(@Param("id") String id);

  void delete(@Param("id") String id);
}
