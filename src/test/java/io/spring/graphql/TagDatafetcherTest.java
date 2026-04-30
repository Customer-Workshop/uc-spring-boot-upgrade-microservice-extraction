package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;
  @InjectMocks private TagDatafetcher fetcher;

  @Test
  void should_get_tags() {
    when(tagsQueryService.allTags()).thenReturn(List.of("java", "spring", "kotlin"));

    List<String> result = fetcher.getTags();

    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
  }
}
