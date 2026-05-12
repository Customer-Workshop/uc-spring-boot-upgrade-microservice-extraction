package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public ResponseEntity getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTags());
          }
        });
  }

  @GetMapping("/entities")
  public ResponseEntity getTagEntities() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTagEntities());
          }
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity getTag(@PathVariable("id") String id) {
    return tagsQueryService
        .findById(id)
        .map(tag -> ResponseEntity.ok(tagResponse(tag)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity createTag(@Valid @RequestBody TagParam tagParam) {
    Tag tag = tagsQueryService.createTag(tagParam.getName());
    return ResponseEntity.status(201).body(tagResponse(tag));
  }

  @PutMapping("/{id}")
  public ResponseEntity updateTag(
      @PathVariable("id") String id, @Valid @RequestBody TagParam tagParam) {
    try {
      Tag tag = tagsQueryService.updateTag(id, tagParam.getName());
      return ResponseEntity.ok(tagResponse(tag));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity deleteTag(@PathVariable("id") String id) {
    if (tagsQueryService.findById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    tagsQueryService.deleteTag(id);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> tagResponse(Tag tag) {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> tagMap = new HashMap<>();
    tagMap.put("id", tag.getId());
    tagMap.put("name", tag.getName());
    response.put("tag", tagMap);
    return response;
  }
}

@Getter
@JsonRootName("tag")
@NoArgsConstructor
@AllArgsConstructor
class TagParam {
  @NotBlank(message = "can't be empty")
  private String name;
}
