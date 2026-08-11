package io.spring.gatling;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Sustained load test of the articles REST API with a realistic read heavy traffic mix.
 *
 * <p>Reads are split evenly between listing articles and reading a single article. Deletes target
 * throwaway articles created during setup so the run is repeatable and never depletes seed data.
 */
public class ArticlesLoadSimulation extends Simulation {

  private static final String BASE_URL = property("baseUrl", "http://localhost:8080");
  private static final double RATE = Double.parseDouble(property("rate", "30"));
  private static final int DURATION_SECONDS = Integer.parseInt(property("durationSeconds", "300"));
  private static final double READ_WEIGHT = Double.parseDouble(property("readWeight", "80"));
  private static final double WRITE_WEIGHT = Double.parseDouble(property("writeWeight", "15"));
  private static final double DELETE_WEIGHT = Double.parseDouble(property("deleteWeight", "5"));
  private static final int P95_SLO_MS = Integer.parseInt(property("p95Ms", "200"));
  private static final double ERROR_RATE_SLO_PERCENT =
      Double.parseDouble(property("errorRatePercent", "1"));
  private static final String USER_EMAIL = property("userEmail", "john@example.com");
  private static final String USER_PASSWORD = property("userPassword", "password123");

  private static final int PAGE_SIZE = 20;
  private static final int WARM_UP_REQUESTS = 50;
  /** Head room so a slightly faster than requested injection cannot exhaust the delete fixtures. */
  private static final double DELETE_FIXTURE_HEAD_ROOM = 1.25;

  private final ConduitApiClient client = new ConduitApiClient(BASE_URL);
  private final String token = client.login(USER_EMAIL, USER_PASSWORD);

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl(BASE_URL)
          .acceptHeader("application/json")
          .contentTypeHeader("application/json")
          .header("Authorization", "Token " + token)
          .userAgentHeader("gatling/articles-load-simulation")
          .disableCaching();

  private final ChainBuilder listArticles =
      exec(session -> session.set("offset", ThreadLocalRandom.current().nextInt(0, 3) * PAGE_SIZE))
          .exec(
              http("GET /articles (list)")
                  .get("/articles?limit=" + PAGE_SIZE + "&offset=#{offset}")
                  .check(status().is(200))
                  .check(jmesPath("articlesCount").exists()));

  private final ChainBuilder readArticle =
      exec(
          http("GET /articles/{slug} (read)")
              .get("/articles/#{slug}")
              .check(status().is(200))
              .check(jmesPath("article.slug").isEL("#{slug}")));

  private final ChainBuilder createArticle =
      exec(session -> session.set("title", "load test " + UUID.randomUUID()))
          .exec(
              http("POST /articles (create)")
                  .post("/articles")
                  .body(
                      StringBody(
                          "{\"article\":{\"title\":\"#{title}\",\"description\":\"created by the"
                              + " articles load simulation\",\"body\":\"created by the articles load"
                              + " simulation\",\"tagList\":[\"loadtest\"]}}"))
                  .check(status().is(200))
                  .check(jmesPath("article.slug").exists()));

  private final ChainBuilder deleteArticle =
      exec(
          http("DELETE /articles/{slug} (delete)")
              .delete("/articles/#{slug}")
              .check(status().is(204)));

  public ArticlesLoadSimulation() {
    List<String> readSlugs = client.existingSlugs(token, PAGE_SIZE);
    client.warmUp(token, readSlugs.get(0), WARM_UP_REQUESTS);

    FeederBuilder<Object> readFeeder = slugFeeder(readSlugs).circular();
    FeederBuilder<Object> deleteFeeder =
        slugFeeder(client.createDisposableArticles(token, deleteFixtureCount())).queue();

    ScenarioBuilder scenario =
        scenario("Articles API traffic mix")
            .randomSwitch()
            .on(
                Choice.withWeight(
                    READ_WEIGHT,
                    randomSwitch()
                        .on(
                            Choice.withWeight(50.0, exec(listArticles)),
                            Choice.withWeight(50.0, feed(readFeeder).exec(readArticle)))),
                Choice.withWeight(WRITE_WEIGHT, createArticle),
                Choice.withWeight(DELETE_WEIGHT, feed(deleteFeeder).exec(deleteArticle)));

    setUp(
            scenario.injectOpen(
                constantUsersPerSec(RATE).during(Duration.ofSeconds(DURATION_SECONDS))))
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile(95).lt(P95_SLO_MS),
            global().failedRequests().percent().lt(ERROR_RATE_SLO_PERCENT),
            details("GET /articles (list)").responseTime().percentile(95).lt(P95_SLO_MS),
            details("GET /articles/{slug} (read)").responseTime().percentile(95).lt(P95_SLO_MS),
            details("POST /articles (create)").responseTime().percentile(95).lt(P95_SLO_MS),
            details("DELETE /articles/{slug} (delete)")
                .responseTime()
                .percentile(95)
                .lt(P95_SLO_MS));
  }

  private static int deleteFixtureCount() {
    double totalWeight = READ_WEIGHT + WRITE_WEIGHT + DELETE_WEIGHT;
    double deletes = RATE * DURATION_SECONDS * DELETE_WEIGHT / totalWeight;
    return (int) Math.ceil(deletes * DELETE_FIXTURE_HEAD_ROOM) + 20;
  }

  private static FeederBuilder<Object> slugFeeder(List<String> slugs) {
    List<Map<String, Object>> records =
        slugs.stream()
            .map(
                slug -> {
                  Map<String, Object> record = new HashMap<String, Object>();
                  record.put("slug", slug);
                  return record;
                })
            .collect(Collectors.toList());
    return listFeeder(records);
  }

  private static String property(String name, String defaultValue) {
    String value = System.getProperty(name);
    return value == null || value.isEmpty() ? defaultValue : value;
  }
}
