package io.spring.comments.infrastructure.mybatis.readservice;

import io.spring.comments.core.comment.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

@Mapper
public interface CommentReadService {

  List<Comment> findByArticleId(@Param("articleId") String articleId);

  List<Comment> findByArticleIdWithCursor(
      @Param("articleId") String articleId,
      @Param("cursor") DateTime cursor,
      @Param("direction") String direction);
}
