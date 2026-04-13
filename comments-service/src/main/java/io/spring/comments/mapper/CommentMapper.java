package io.spring.comments.mapper;

import io.spring.comments.model.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  void insert(@Param("comment") Comment comment);

  Comment findById(@Param("id") String id);

  Comment findByIdAndArticleId(@Param("id") String id, @Param("articleId") String articleId);

  List<Comment> findByArticleId(@Param("articleId") String articleId);

  void delete(@Param("id") String id);
}
