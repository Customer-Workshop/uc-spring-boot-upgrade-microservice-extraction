package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagsQueryServiceTest {

  @Mock private TagReadService tagReadService;

  private TagsQueryService tagsQueryService;

  @BeforeEach
  public void setUp() {
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  public void should_return_all_tags() {
    when(tagReadService.all()).thenReturn(Arrays.asList("java", "spring", "testing"));
    List<String> tags = tagsQueryService.allTags();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("java"));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());
    List<String> tags = tagsQueryService.allTags();
    assertTrue(tags.isEmpty());
  }
}
