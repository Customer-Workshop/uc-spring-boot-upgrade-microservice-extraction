package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the internal profile endpoint the comments microservice calls back into to enrich
 * comment authors.
 */
@WebMvcTest(InternalProfileApi.class)
@Import({WebSecurityConfig.class})
public class InternalProfileApiTest {

  @MockBean private UserReadService userReadService;
  @MockBean private UserRelationshipQueryService userRelationshipQueryService;
  @MockBean private io.spring.core.user.UserRepository userRepository;
  @MockBean private io.spring.core.service.JwtService jwtService;

  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_return_profile_with_following_flag() {
    when(userReadService.findById(eq("user-1")))
        .thenReturn(new UserData("user-1", "a@test.com", "alice", "bio", "img"));
    when(userRelationshipQueryService.isUserFollowing(eq("viewer-1"), eq("user-1")))
        .thenReturn(true);

    given()
        .when()
        .get("/internal/profiles?userId=user-1&viewerId=viewer-1")
        .then()
        .statusCode(200)
        .body("id", equalTo("user-1"))
        .body("username", equalTo("alice"))
        .body("following", equalTo(true));
  }

  @Test
  public void should_return_404_when_user_missing() {
    when(userReadService.findById(eq("missing"))).thenReturn(null);

    given().when().get("/internal/profiles?userId=missing").then().statusCode(404);
  }
}
