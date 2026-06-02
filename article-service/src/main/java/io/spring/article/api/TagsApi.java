package io.spring.article.api;

import io.spring.article.application.TagsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public ResponseEntity<?> getTags() {
    return ResponseEntity.ok(tagsResponse(tagsQueryService));
  }

  private Map<String, Object> tagsResponse(TagsQueryService tagsQueryService) {
    return new HashMap<String, Object>() {
      {
        put("tags", tagsQueryService.allTags());
      }
    };
  }
}
