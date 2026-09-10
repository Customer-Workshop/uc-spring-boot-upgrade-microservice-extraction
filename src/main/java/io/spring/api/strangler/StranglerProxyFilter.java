package io.spring.api.strangler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forwards the comment and favorite routes to the extracted services when their base URLs are
 * configured, and lets the request fall through to the in-process controllers when they are not.
 */
public class StranglerProxyFilter extends OncePerRequestFilter {
  private static final Pattern COMMENTS = Pattern.compile("^/articles/[^/]+/comments(/.*)?$");
  private static final Pattern FAVORITE = Pattern.compile("^/articles/[^/]+/favorite$");
  private static final List<String> FORWARDED_REQUEST_HEADERS =
      Arrays.asList("authorization", "content-type", "accept");

  private final StranglerProperties properties;
  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;

  public StranglerProxyFilter(
      StranglerProperties properties, int connectTimeoutMillis, int readTimeoutMillis) {
    this.properties = properties;
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.readTimeoutMillis = readTimeoutMillis;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return targetBaseUrl(request.getRequestURI()) == null;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String baseUrl = targetBaseUrl(request.getRequestURI());
    if (baseUrl == null) {
      filterChain.doFilter(request, response);
      return;
    }
    proxy(baseUrl, request, response);
  }

  private String targetBaseUrl(String path) {
    if (properties.isCommentsRouted() && COMMENTS.matcher(path).matches()) {
      return properties.getCommentsServiceUrl();
    }
    if (properties.isFavoritesRouted() && FAVORITE.matcher(path).matches()) {
      return properties.getFavoritesServiceUrl();
    }
    return null;
  }

  private void proxy(String baseUrl, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String query = request.getQueryString();
    URL target = new URL(baseUrl + request.getRequestURI() + (query == null ? "" : "?" + query));
    HttpURLConnection connection = (HttpURLConnection) target.openConnection();
    connection.setInstanceFollowRedirects(false);
    connection.setConnectTimeout(connectTimeoutMillis);
    connection.setReadTimeout(readTimeoutMillis);
    connection.setRequestMethod(request.getMethod());
    for (String header : FORWARDED_REQUEST_HEADERS) {
      String value = request.getHeader(header);
      if (value != null) {
        connection.setRequestProperty(header, value);
      }
    }

    byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
    int status;
    try {
      if (body.length > 0) {
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(body.length);
        try (OutputStream out = connection.getOutputStream()) {
          out.write(body);
        }
      }
      status = connection.getResponseCode();
    } catch (IOException e) {
      logger.error("strangler proxy to " + target + " failed", e);
      response.sendError(
          HttpStatus.BAD_GATEWAY.value(), "downstream service unavailable: " + baseUrl);
      return;
    }

    response.setStatus(status);
    for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
      String name = header.getKey();
      if (name == null || isHopByHop(name)) {
        continue;
      }
      for (String value : header.getValue()) {
        response.addHeader(name, value);
      }
    }

    try (InputStream in =
        status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
      if (in != null) {
        StreamUtils.copy(in, response.getOutputStream());
      }
    } finally {
      connection.disconnect();
    }
    response.flushBuffer();
  }

  private boolean isHopByHop(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.equals("transfer-encoding")
        || lower.equals("connection")
        || lower.equals("keep-alive")
        || lower.equals("content-length");
  }
}
