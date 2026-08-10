package io.spring.cucumber;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.springframework.core.env.Environment;

/** Thin RestAssured wrapper around the running application. */
public class ApiClient {

  private final Environment environment;

  public ApiClient(Environment environment) {
    this.environment = environment;
  }

  /** Resolved lazily: the random port is only known once the servlet container has started. */
  private String baseUri() {
    return "http://localhost:" + environment.getRequiredProperty("local.server.port");
  }

  public Response get(String path, Map<String, ?> queryParams, String token) {
    return request(token).queryParams(queryParams).get(baseUri() + path);
  }

  public Response post(String path, Object body, String token) {
    RequestSpecification spec = request(token);
    if (body != null) {
      spec.body(body);
    }
    return spec.post(baseUri() + path);
  }

  public Response delete(String path, String token) {
    return request(token).delete(baseUri() + path);
  }

  private RequestSpecification request(String token) {
    RequestSpecification spec =
        RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON);
    if (token != null) {
      spec.header("Authorization", "Token " + token);
    }
    return spec;
  }
}
