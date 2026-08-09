package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Handles tag operations under {@code /tags}. */
@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  /**
   * Handles {@code GET /tags} and returns the tags envelope.
   *
   * @return a response containing all tag names
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
