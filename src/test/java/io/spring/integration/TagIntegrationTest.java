package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

public class TagIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testGetTags() {
    given().when().get("/tags").then().statusCode(200).body("tags", notNullValue());
  }
}
