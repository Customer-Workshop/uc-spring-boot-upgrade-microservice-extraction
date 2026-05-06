package io.spring.articleservice.api;

import io.spring.articleservice.application.data.CommentData;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.comment.Comment;
import io.spring.articleservice.core.comment.CommentRepository;
import io.spring.articleservice.infrastructure.mybatis.readservice.CommentReadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentReadService commentReadService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Comment comment = new Comment(newCommentParam.getBody(), userId, article.getId());
              commentRepository.save(comment);
              CommentData commentData = commentReadService.findById(comment.getId());
              return ResponseEntity.ok(commentResponse(commentData));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              List<CommentData> comments = commentReadService.findByArticleId(article.getId());
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("comments", comments);
                    }
                  });
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                commentRepository
                    .findById(article.getId(), commentId)
                    .map(
                        comment -> {
                          if (!comment.getUserId().equals(userId)) {
                            return ResponseEntity.status(403).build();
                          }
                          commentRepository.remove(comment);
                          return ResponseEntity.noContent().build();
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
