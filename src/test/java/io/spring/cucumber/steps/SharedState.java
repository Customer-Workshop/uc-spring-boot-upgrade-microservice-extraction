package io.spring.cucumber.steps;

import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class SharedState {
  private Response lastResponse;
  private String authToken;
  private String freshUserAuthToken;
  private String articleSlug;
  private int initialFavoritesCount;
  private Response firstPageResponse;

  public Response getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(Response lastResponse) {
    this.lastResponse = lastResponse;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getFreshUserAuthToken() {
    return freshUserAuthToken;
  }

  public void setFreshUserAuthToken(String freshUserAuthToken) {
    this.freshUserAuthToken = freshUserAuthToken;
  }

  public String getArticleSlug() {
    return articleSlug;
  }

  public void setArticleSlug(String articleSlug) {
    this.articleSlug = articleSlug;
  }

  public int getInitialFavoritesCount() {
    return initialFavoritesCount;
  }

  public void setInitialFavoritesCount(int initialFavoritesCount) {
    this.initialFavoritesCount = initialFavoritesCount;
  }

  public Response getFirstPageResponse() {
    return firstPageResponse;
  }

  public void setFirstPageResponse(Response firstPageResponse) {
    this.firstPageResponse = firstPageResponse;
  }
}
