package io.spring.articleservice.application;

import io.spring.articleservice.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  public List<String> allTags() {
    return tagReadService.all();
  }
}
