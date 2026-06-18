package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CommentReadServiceInterface;
import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import java.util.List;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!microservice")
@Primary
public class LocalCommentReadService implements CommentReadServiceInterface {
  private final CommentReadService commentReadService;

  public LocalCommentReadService(CommentReadService commentReadService) {
    this.commentReadService = commentReadService;
  }

  @Override
  public CommentData findById(String id) {
    return commentReadService.findById(id);
  }

  @Override
  public List<CommentData> findByArticleId(String articleId) {
    return commentReadService.findByArticleId(articleId);
  }

  @Override
  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<DateTime> page) {
    return commentReadService.findByArticleIdWithCursor(articleId, page);
  }
}
