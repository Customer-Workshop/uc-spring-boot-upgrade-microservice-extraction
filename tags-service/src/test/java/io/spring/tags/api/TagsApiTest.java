package io.spring.tags.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.tags.application.TagsQueryService;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
public class TagsApiTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TagsQueryService tagsQueryService;

  @Test
  public void should_get_tags_success() throws Exception {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring"));
    mockMvc
        .perform(get("/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags[0]").value("java"))
        .andExpect(jsonPath("$.tags[1]").value("spring"));
  }

  @Test
  public void should_get_empty_tags() throws Exception {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList());
    mockMvc.perform(get("/tags")).andExpect(status().isOk()).andExpect(jsonPath("$.tags").isEmpty());
  }
}
