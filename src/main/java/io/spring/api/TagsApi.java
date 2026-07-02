package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for retrieving all available article tags. */
@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  /**
   * Retrieves all tags that have been applied to articles.
   *
   * @return a list of tag names wrapped in a {@code {"tags": [...]}} envelope
   */
  @GetMapping
  public ResponseEntity getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTags());
          }
        });
  }
}
