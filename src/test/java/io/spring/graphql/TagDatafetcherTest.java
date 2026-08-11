package io.spring.graphql;

import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  @Test
  public void should_get_all_tags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring"));

    Assertions.assertEquals(
        Arrays.asList("java", "spring"), new TagDatafetcher(tagsQueryService).getTags());
  }

  @Test
  public void should_get_empty_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    Assertions.assertTrue(new TagDatafetcher(tagsQueryService).getTags().isEmpty());
  }
}
