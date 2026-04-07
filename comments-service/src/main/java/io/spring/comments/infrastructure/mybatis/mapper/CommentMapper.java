package io.spring.comments.infrastructure.mybatis.mapper;

import io.spring.comments.core.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  void insert(@Param("comment") Comment comment);

  Comment findById(@Param("id") String id);

  Comment findByArticleIdAndId(@Param("articleId") String articleId, @Param("id") String id);

  List<Comment> findByArticleId(@Param("articleId") String articleId);

  void delete(@Param("id") String id);
}
