package io.spring.cucumber;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

public class CucumberHooks {

  @LocalServerPort private int port;

  @Autowired private io.spring.cucumber.steps.SharedState sharedState;

  @Before(order = 0)
  public void setUpRestAssured() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  @Before(order = 1)
  public void resetSharedState() {
    sharedState.setLastResponse(null);
    sharedState.setAuthToken(null);
    sharedState.setFreshUserAuthToken(null);
    sharedState.setArticleSlug(null);
    sharedState.setInitialFavoritesCount(0);
    sharedState.setFirstPageResponse(null);
  }
}
