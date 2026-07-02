package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

/** MyBatis mapper for read-side comment queries. */
@Mapper
public interface CommentReadService {

  /** Finds a single comment's read-model data by its ID. */
  CommentData findById(@Param("id") String id);

  /** Finds all comments for a given article, ordered by creation date. */
  List<CommentData> findByArticleId(@Param("articleId") String articleId);

  /** Finds comments for a given article with cursor-based pagination. */
  List<CommentData> findByArticleIdWithCursor(
      @Param("articleId") String articleId, @Param("page") CursorPageParameter<DateTime> page);
}
