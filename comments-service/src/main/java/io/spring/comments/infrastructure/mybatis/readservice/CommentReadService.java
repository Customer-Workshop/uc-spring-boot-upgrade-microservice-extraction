package io.spring.comments.infrastructure.mybatis.readservice;

import io.spring.comments.application.CursorPageParameter;
import io.spring.comments.core.comment.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

@Mapper
public interface CommentReadService {
  Comment findById(@Param("id") String id);

  List<Comment> findByArticleId(@Param("articleId") String articleId);

  List<Comment> findByArticleIdWithCursor(
      @Param("articleId") String articleId, @Param("page") CursorPageParameter<DateTime> page);
}
