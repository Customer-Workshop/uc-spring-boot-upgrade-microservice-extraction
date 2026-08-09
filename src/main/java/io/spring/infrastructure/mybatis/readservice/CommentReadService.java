package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

/** Reads comment projections joined with their author profiles. */
@Mapper
public interface CommentReadService {
  /** Finds a comment projection by identifier. */
  CommentData findById(@Param("id") String id);

  /** Finds all comment projections for an article. */
  List<CommentData> findByArticleId(@Param("articleId") String articleId);

  /** Finds article comments around a creation-time cursor. */
  List<CommentData> findByArticleIdWithCursor(
      @Param("articleId") String articleId, @Param("page") CursorPageParameter<DateTime> page);
}
