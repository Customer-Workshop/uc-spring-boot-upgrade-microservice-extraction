package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.infrastructure.service.TagServiceClient;
import java.util.List;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class TagDatafetcher {
  private TagServiceClient tagServiceClient;

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Tags)
  public List<String> getTags() {
    return tagServiceClient.getAllTags();
  }
}
