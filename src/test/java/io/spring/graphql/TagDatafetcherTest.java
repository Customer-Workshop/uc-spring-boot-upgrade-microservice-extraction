package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  @Test
  void should_return_all_tags() {
    TagDatafetcher fetcher = new TagDatafetcher(tagsQueryService);
    List<String> tags = Arrays.asList("java", "spring", "react");
    when(tagsQueryService.allTags()).thenReturn(tags);

    List<String> result = fetcher.getTags();

    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
  }

  @Test
  void should_return_empty_list_when_no_tags() {
    TagDatafetcher fetcher = new TagDatafetcher(tagsQueryService);
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> result = fetcher.getTags();

    assertTrue(result.isEmpty());
  }
}
