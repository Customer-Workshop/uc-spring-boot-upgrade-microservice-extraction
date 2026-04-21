package io.spring.article.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import io.spring.article.application.TagsQueryService;
import io.spring.article.graphql.DgsConstants.QUERY;
import java.util.List;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class TagDatafetcher {
  private TagsQueryService tagsQueryService;

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Tags)
  public List<String> getTags() {
    return tagsQueryService.allTags();
  }
}
