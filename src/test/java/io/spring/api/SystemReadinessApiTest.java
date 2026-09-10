package io.spring.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.SystemReadinessQueryService;
import io.spring.application.data.ReadinessData;
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

  @Autowired private SystemReadinessQueryService systemReadinessQueryService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_readiness_without_token() throws Exception {
    ReadinessData data = systemReadinessQueryService.readiness();
    RestAssuredMockMvc.when()
        .get("/system/readiness")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("$", hasKey("readiness"))
        .body("readiness.checks", hasSize(5))
        .body("readiness.summary.total", equalTo(data.getChecks().size()))
        .body("readiness.checks.status", everyItem(is(in(new String[] {"PASS", "FAIL"}))));
  }
}
