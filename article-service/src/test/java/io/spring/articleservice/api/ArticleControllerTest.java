package io.spring.articleservice.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateAndGetArticle() throws Exception {
    CreateArticleRequest request = new CreateArticleRequest();
    request.setTitle("Test Article");
    request.setDescription("Test Description");
    request.setBody("Test Body Content");
    request.setTagList(List.of("java", "spring"));
    request.setUserId("user-123");

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title", is("Test Article")))
        .andExpect(jsonPath("$.slug", is("test-article")))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.tagList", hasSize(2)));

    mockMvc
        .perform(get("/api/articles/test-article"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is("Test Article")));
  }

  @Test
  void shouldUpdateArticle() throws Exception {
    CreateArticleRequest create = new CreateArticleRequest();
    create.setTitle("Original Title");
    create.setDescription("Description");
    create.setBody("Body");
    create.setUserId("user-456");

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
        .andExpect(status().isCreated());

    UpdateArticleRequest update = new UpdateArticleRequest();
    update.setTitle("Updated Title");

    mockMvc
        .perform(
            put("/api/articles/original-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is("Updated Title")));
  }

  @Test
  void shouldDeleteArticle() throws Exception {
    CreateArticleRequest create = new CreateArticleRequest();
    create.setTitle("To Delete");
    create.setDescription("Description");
    create.setBody("Body");
    create.setUserId("user-789");

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
        .andExpect(status().isCreated());

    mockMvc.perform(delete("/api/articles/to-delete")).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/articles/to-delete")).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnNotFoundForMissingArticle() throws Exception {
    mockMvc.perform(get("/api/articles/nonexistent-slug")).andExpect(status().isNotFound());
  }

  @Test
  void shouldListArticles() throws Exception {
    mockMvc.perform(get("/api/articles")).andExpect(status().isOk());
  }

  @Test
  void shouldGetTags() throws Exception {
    mockMvc.perform(get("/api/articles/tags")).andExpect(status().isOk());
  }
}
