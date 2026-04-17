package io.spring.article.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.article.api.dto.CreateArticleRequest;
import io.spring.article.api.dto.UpdateArticleRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArticleControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateArticle() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body Content")
            .tagList(List.of("java", "spring"))
            .userId("user-123")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Test Article"))
        .andExpect(jsonPath("$.description").value("Test Description"))
        .andExpect(jsonPath("$.body").value("Test Body Content"))
        .andExpect(jsonPath("$.slug").value("test-article"))
        .andExpect(jsonPath("$.userId").value("user-123"));
  }

  @Test
  void shouldGetArticleBySlug() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Fetch Me Article")
            .description("Description")
            .body("Body")
            .tagList(List.of())
            .userId("user-456")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/articles/fetch-me-article"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Fetch Me Article"))
        .andExpect(jsonPath("$.userId").value("user-456"));
  }

  @Test
  void shouldReturn404ForNonExistentArticle() throws Exception {
    mockMvc.perform(get("/api/articles/non-existent-slug")).andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateArticle() throws Exception {
    CreateArticleRequest createRequest =
        CreateArticleRequest.builder()
            .title("Update Me Article")
            .description("Old Description")
            .body("Old Body")
            .tagList(List.of())
            .userId("user-789")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated());

    UpdateArticleRequest updateRequest =
        UpdateArticleRequest.builder()
            .title("Updated Title")
            .description("Updated Description")
            .body("Updated Body")
            .build();

    mockMvc
        .perform(
            put("/api/articles/update-me-article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"))
        .andExpect(jsonPath("$.description").value("Updated Description"))
        .andExpect(jsonPath("$.body").value("Updated Body"));
  }

  @Test
  void shouldDeleteArticle() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Delete Me Article")
            .description("Description")
            .body("Body")
            .tagList(List.of())
            .userId("user-delete")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc.perform(delete("/api/articles/delete-me-article")).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/articles/delete-me-article")).andExpect(status().isNotFound());
  }
}
