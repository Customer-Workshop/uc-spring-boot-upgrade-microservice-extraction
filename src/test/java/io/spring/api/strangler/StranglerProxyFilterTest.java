package io.spring.api.strangler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class StranglerProxyFilterTest {

  private HttpServer server;
  private final List<String> receivedPaths = new ArrayList<>();
  private final List<String> receivedBodies = new ArrayList<>();
  private final List<String> receivedAuth = new ArrayList<>();

  @BeforeEach
  public void startStubService() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          receivedPaths.add(
              exchange.getRequestURI().getPath()
                  + (exchange.getRequestURI().getQuery() == null
                      ? ""
                      : "?" + exchange.getRequestURI().getQuery()));
          receivedBodies.add(
              new String(
                  org.springframework.util.StreamUtils.copyToByteArray(exchange.getRequestBody()),
                  StandardCharsets.UTF_8));
          receivedAuth.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
          byte[] body = "{\"comment\":{\"id\":\"1\"}}".getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(201, body.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
          }
        });
    server.start();
  }

  @AfterEach
  public void stopStubService() {
    server.stop(0);
  }

  private String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  private StranglerProxyFilter filter(String commentsUrl, String favoritesUrl) {
    return new StranglerProxyFilter(new StranglerProperties(commentsUrl, favoritesUrl), 2000, 5000);
  }

  @Test
  public void should_serve_in_process_when_no_service_url_is_configured() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/articles/slug/comments");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter("", "").doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertTrue(receivedPaths.isEmpty());
  }

  @Test
  public void should_forward_comment_routes_when_url_is_configured() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/articles/slug/comments");
    request.setContent("{\"comment\":{\"body\":\"hi\"}}".getBytes(StandardCharsets.UTF_8));
    request.addHeader("Authorization", "Token jwt");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter(baseUrl(), "").doFilter(request, response, chain);

    verifyNoInteractions(chain);
    assertEquals(201, response.getStatus());
    assertEquals("{\"comment\":{\"id\":\"1\"}}", response.getContentAsString());
    assertEquals("/articles/slug/comments", receivedPaths.get(0));
    assertEquals("{\"comment\":{\"body\":\"hi\"}}", receivedBodies.get(0));
    assertEquals("Token jwt", receivedAuth.get(0));
  }

  @Test
  public void should_forward_nested_comment_routes_and_query_string() throws Exception {
    MockHttpServletRequest request =
        new MockHttpServletRequest("DELETE", "/articles/slug/comments/123");
    request.setQueryString("dry=true");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter(baseUrl(), "").doFilter(request, response, mock(FilterChain.class));

    assertEquals("/articles/slug/comments/123?dry=true", receivedPaths.get(0));
  }

  @Test
  public void should_forward_favorite_route_to_the_favorites_service() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/articles/slug/favorite");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter("", baseUrl()).doFilter(request, response, mock(FilterChain.class));

    assertEquals("/articles/slug/favorite", receivedPaths.get(0));
  }

  @Test
  public void should_not_forward_unrelated_article_routes() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/articles/slug");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter(baseUrl(), baseUrl()).doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertTrue(receivedPaths.isEmpty());
  }

  @Test
  public void should_answer_502_when_the_service_is_unreachable() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/articles/slug/comments");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // port 1 is closed; the connection attempt fails fast
    filter("http://127.0.0.1:1", "").doFilter(request, response, mock(FilterChain.class));

    assertEquals(502, response.getStatus());
  }
}
