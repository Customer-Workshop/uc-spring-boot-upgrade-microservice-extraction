package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import java.util.HashMap;
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
  private TagRepository tagRepository;

  @GetMapping
  public ResponseEntity getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTags());
          }
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity getTag(@PathVariable("id") String id) {
    return tagRepository
        .findById(id)
        .map(
            tag ->
                ResponseEntity.ok(
                    new HashMap<String, Object>() {
                      {
                        put("tag", tag);
                      }
                    }))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping
  public ResponseEntity createTag(@Valid @RequestBody TagParam tagParam) {
    Tag tag = new Tag(tagParam.getName());
    tagRepository.save(tag);
    return ResponseEntity.status(201)
        .body(
            new HashMap<String, Object>() {
              {
                put("tag", tag);
              }
            });
  }

  @PutMapping("/{id}")
  public ResponseEntity updateTag(
      @PathVariable("id") String id, @Valid @RequestBody TagParam tagParam) {
    return tagRepository
        .findById(id)
        .map(
            tag -> {
              tag.update(tagParam.getName());
              tagRepository.save(tag);
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("tag", tag);
                    }
                  });
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity deleteTag(@PathVariable("id") String id) {
    return tagRepository
        .findById(id)
        .map(
            tag -> {
              tagRepository.removeArticleTagsByTagId(id);
              tagRepository.remove(id);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("tag")
class TagParam {
  @NotBlank private String name;
}
