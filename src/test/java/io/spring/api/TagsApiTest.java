package io.spring.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class TagsApiTest {

  @Mock private TagsQueryService tagsQueryService;

  @InjectMocks private TagsApi tagsApi;

  @Test
  void should_return_tags_list() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "kotlin"));

    ResponseEntity response = tagsApi.getTags();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_return_empty_tags_list() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    ResponseEntity response = tagsApi.getTags();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_call_tags_query_service() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java"));

    tagsApi.getTags();

    verify(tagsQueryService).allTags();
  }
}
