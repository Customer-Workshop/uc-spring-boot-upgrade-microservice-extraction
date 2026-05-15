package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TagsQueryServiceTest2 {

  private TagReadService tagReadService;
  private TagsQueryService tagsQueryService;

  @BeforeEach
  public void setUp() {
    tagReadService = mock(TagReadService.class);
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  public void should_return_all_tags() {
    when(tagReadService.all()).thenReturn(Arrays.asList("java", "spring", "react"));

    List<String> tags = tagsQueryService.allTags();

    assertEquals(3, tags.size());
    assertTrue(tags.contains("java"));
    assertTrue(tags.contains("spring"));
    assertTrue(tags.contains("react"));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());

    List<String> tags = tagsQueryService.allTags();

    assertTrue(tags.isEmpty());
  }
}
