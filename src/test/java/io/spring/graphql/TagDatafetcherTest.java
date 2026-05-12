package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  @InjectMocks private TagDatafetcher tagDatafetcher;

  @Test
  void should_get_tags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "react"));

    List<String> tags = tagDatafetcher.getTags();

    assertEquals(3, tags.size());
    assertTrue(tags.contains("java"));
  }

  @Test
  void should_return_empty_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> tags = tagDatafetcher.getTags();

    assertTrue(tags.isEmpty());
  }
}
