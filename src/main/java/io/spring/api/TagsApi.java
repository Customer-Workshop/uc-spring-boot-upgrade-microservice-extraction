package io.spring.api;

import io.spring.application.TagsQueryService;
import io.spring.application.data.TagData;
import io.spring.core.article.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
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

  @GetMapping("/details")
  public ResponseEntity<Map<String, List<TagData>>> getTagDetails() {
    Map<String, List<TagData>> response = new HashMap<>();
    response.put("tags", tagsQueryService.findAll());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity getTagById(@PathVariable("id") String id) {
    Optional<TagData> tagData = tagsQueryService.findById(id);
    if (!tagData.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    Map<String, TagData> response = new HashMap<>();
    response.put("tag", tagData.get());
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity createTag(@Valid @RequestBody TagParam tagParam) {
    Tag tag = tagsQueryService.createTag(tagParam.getName());
    Map<String, Object> response = new HashMap<>();
    response.put("tag", toTagResponse(tag));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity updateTag(
      @PathVariable("id") String id, @Valid @RequestBody TagParam tagParam) {
    Tag tag = tagsQueryService.updateTag(id, tagParam.getName());
    Map<String, Object> response = new HashMap<>();
    response.put("tag", toTagResponse(tag));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity deleteTag(@PathVariable("id") String id) {
    tagsQueryService.removeTag(id);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> toTagResponse(Tag tag) {
    Map<String, Object> tagMap = new HashMap<>();
    tagMap.put("id", tag.getId());
    tagMap.put("name", tag.getName());
    tagMap.put("createdAt", tag.getCreatedAt());
    return tagMap;
  }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonRootName("tag")
class TagParam {
  @NotBlank private String name;
}
