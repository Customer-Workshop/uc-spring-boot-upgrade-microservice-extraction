package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Provides tag-name reads. */
@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  /** Returns all tag names from the tag store. */
  public List<String> allTags() {
    return tagReadService.all();
  }
}
