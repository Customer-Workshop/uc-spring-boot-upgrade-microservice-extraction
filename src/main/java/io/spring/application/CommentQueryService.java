package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.rest.CommentServiceClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Reads comment data from the extracted comments microservice. Author profile enrichment (including
 * the follow relationship) is performed by the microservice via a callback to this monolith.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentServiceClient commentServiceClient;

  public Optional<CommentData> findById(String id, User user) {
    return commentServiceClient.findCommentDataById(id, user == null ? null : user.getId());
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    return commentServiceClient.findByArticleId(articleId, user == null ? null : user.getId());
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentData> comments =
        commentServiceClient.findByArticleIdWithCursor(
            articleId,
            user == null ? null : user.getId(),
            page.getCursor(),
            page.getQueryLimit(),
            page.isNext());
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }
}
