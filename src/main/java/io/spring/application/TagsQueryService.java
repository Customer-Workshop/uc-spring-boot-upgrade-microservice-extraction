package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Read-side service for querying article tags. */
@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  /**
   * Retrieves all distinct tag names across all articles.
   *
   * @return list of tag name strings
   */
  public List<String> allTags() {
    return tagReadService.all();
  }
}
