package io.spring.commentservice.application;

import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;

  public Optional<CommentData> findById(String id) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    }
    return Optional.of(commentData);
  }

  public List<CommentData> findByArticleId(String articleId) {
    return commentReadService.findByArticleId(articleId);
  }
}
