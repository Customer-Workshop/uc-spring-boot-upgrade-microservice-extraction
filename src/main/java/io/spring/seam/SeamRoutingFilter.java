package io.spring.seam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SeamRoutingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(SeamRoutingFilter.class);

  private final SeamProperties seamProperties;
  private final DomainClassifier domainClassifier;

  public SeamRoutingFilter(SeamProperties seamProperties, DomainClassifier domainClassifier) {
    this.seamProperties = seamProperties;
    this.domainClassifier = domainClassifier;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String path = httpRequest.getRequestURI();
    String domain = domainClassifier.classify(path);

    if (domain == null) {
      chain.doFilter(request, response);
      return;
    }

    RoutingMode mode = seamProperties.getModeForDomain(domain);

    if (mode == RoutingMode.MONOLITH) {
      log.debug("Seam: routing {} to MONOLITH (domain={})", path, domain);
      chain.doFilter(request, response);
      return;
    }

    String microserviceUrl = seamProperties.getUrlForDomain(domain);
    if (microserviceUrl == null || microserviceUrl.isEmpty()) {
      log.warn(
          "Seam: domain {} configured for MICROSERVICE but no URL set, falling back to MONOLITH",
          domain);
      chain.doFilter(request, response);
      return;
    }

    log.info("Seam: routing {} to MICROSERVICE at {} (domain={})", path, microserviceUrl, domain);
    proxyRequest(httpRequest, httpResponse, microserviceUrl);
  }

  private void proxyRequest(
      HttpServletRequest request, HttpServletResponse response, String baseUrl) throws IOException {

    String targetUrl = baseUrl + request.getRequestURI();
    String queryString = request.getQueryString();
    if (queryString != null) {
      targetUrl += "?" + queryString;
    }

    HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
    connection.setRequestMethod(request.getMethod());
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(30000);

    List<String> headerNames = Collections.list(request.getHeaderNames());
    for (String headerName : headerNames) {
      if ("host".equalsIgnoreCase(headerName)) {
        continue;
      }
      List<String> values = Collections.list(request.getHeaders(headerName));
      for (String value : values) {
        connection.addRequestProperty(headerName, value);
      }
    }

    if ("POST".equalsIgnoreCase(request.getMethod())
        || "PUT".equalsIgnoreCase(request.getMethod())
        || "PATCH".equalsIgnoreCase(request.getMethod())) {
      connection.setDoOutput(true);
      try (InputStream in = request.getInputStream();
          OutputStream out = connection.getOutputStream()) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
    }

    int statusCode = connection.getResponseCode();
    response.setStatus(statusCode);

    connection
        .getHeaderFields()
        .forEach(
            (name, values) -> {
              if (name != null
                  && !"Transfer-Encoding".equalsIgnoreCase(name)
                  && !"Content-Length".equalsIgnoreCase(name)) {
                for (String value : values) {
                  response.addHeader(name, value);
                }
              }
            });

    InputStream responseStream =
        statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
    if (responseStream != null) {
      try (InputStream in = responseStream;
          OutputStream out = response.getOutputStream()) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }
}
