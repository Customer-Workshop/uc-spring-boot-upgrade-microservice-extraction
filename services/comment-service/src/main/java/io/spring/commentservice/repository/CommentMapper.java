package io.spring.commentservice.repository;

import io.spring.commentservice.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  void insert(@Param("comment") Comment comment);

  void delete(@Param("id") String id);

  Comment findById(@Param("id") String id, @Param("articleId") String articleId);
}
