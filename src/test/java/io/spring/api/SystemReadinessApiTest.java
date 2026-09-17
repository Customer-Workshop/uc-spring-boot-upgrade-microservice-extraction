package io.spring.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.SystemReadinessQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SystemReadinessApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class, SystemReadinessQueryService.class})
public class SystemReadinessApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_readiness_without_token() throws Exception {
    RestAssuredMockMvc.when()
        .get("/system/readiness")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("readiness", notNullValue())
        .body("readiness.runtime.javaVersion", notNullValue())
        .body("readiness.checks", hasSize(5))
        .body("readiness.checks.id[0]", equalTo("java-17-runtime"))
        .body("readiness.checks.id[4]", equalTo("websecurity-adapter"))
        .body("readiness.checks.status", everyItem(oneOf("PASS", "FAIL")))
        .body("readiness.summary.total", equalTo(5));
  }
}
