package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagStatsData;
import io.spring.application.TagsQueryService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TagsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private TagsQueryService tagsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_tag_stats_without_authentication() {
    when(tagsQueryService.tagStats())
        .thenReturn(Arrays.asList(new TagStatsData("java", 12), new TagStatsData("spring", 7)));

    when()
        .get("/tags/stats")
        .then()
        .statusCode(200)
        .body("tags[0].name", equalTo("java"))
        .body("tags[0].articleCount", equalTo(12))
        .body("tags[1].name", equalTo("spring"))
        .body("tags[1].articleCount", equalTo(7));
  }
}
