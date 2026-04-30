package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TagDatafetcherTest {

  @Test
  public void should_return_tags() {
    TagsQueryService tagsQueryService = Mockito.mock(TagsQueryService.class);
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "test"));
    TagDatafetcher fetcher = new TagDatafetcher(tagsQueryService);

    List<String> result = fetcher.getTags();
    assertEquals(3, result.size());
    assertEquals("java", result.get(0));
  }
}
