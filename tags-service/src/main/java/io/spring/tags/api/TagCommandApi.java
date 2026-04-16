package io.spring.tags.api;

import io.spring.tags.application.TagCommandService;
import io.spring.tags.domain.Tag;
import java.util.HashMap;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagCommandApi {
  private TagCommandService tagCommandService;

  @PostMapping
  public ResponseEntity createTag(@Valid @RequestBody CreateTagRequest request) {
    Tag tag = tagCommandService.findOrCreateTag(request.getName());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("id", tag.getId());
            put("name", tag.getName());
          }
        });
  }

  @PostMapping("/article-tags")
  public ResponseEntity createArticleTagRelation(
      @Valid @RequestBody ArticleTagRequest request) {
    tagCommandService.createArticleTagRelation(request.getArticleId(), request.getTagId());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("articleId", request.getArticleId());
            put("tagId", request.getTagId());
          }
        });
  }

  @Getter
  @NoArgsConstructor
  static class CreateTagRequest {
    @NotBlank private String name;
  }

  @Getter
  @NoArgsConstructor
  static class ArticleTagRequest {
    @NotBlank private String articleId;
    @NotBlank private String tagId;
  }
}
