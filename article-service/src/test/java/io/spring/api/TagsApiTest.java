package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import io.spring.core.service.JwtService;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
class TagsApiTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TagsQueryService tagsQueryService;
  @MockBean private JwtService jwtService;

  @Test
  void shouldGetAllTags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "docker"));

    given()
        .mockMvc(mockMvc)
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags", hasItems("java", "spring", "docker"));
  }
}
