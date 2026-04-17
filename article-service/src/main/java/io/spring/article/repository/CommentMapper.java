package io.spring.article.repository;

import io.spring.article.domain.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  void insert(@Param("comment") Comment comment);

  Comment findById(@Param("articleId") String articleId, @Param("id") String id);

  List<Comment> findByArticleId(@Param("articleId") String articleId);

  void delete(@Param("id") String id);
}
