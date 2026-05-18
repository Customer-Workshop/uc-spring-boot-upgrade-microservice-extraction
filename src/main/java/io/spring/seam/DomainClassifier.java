package io.spring.seam;

import org.springframework.stereotype.Component;

@Component
public class DomainClassifier {

  public static final String DOMAIN_USERS = "users";
  public static final String DOMAIN_ARTICLES = "articles";
  public static final String DOMAIN_COMMENTS = "comments";

  public String classify(String requestPath) {
    if (requestPath == null) {
      return null;
    }

    String path = requestPath.startsWith("/") ? requestPath : "/" + requestPath;

    if (path.startsWith("/users") || path.startsWith("/user") || path.startsWith("/profiles")) {
      return DOMAIN_USERS;
    }

    if (path.matches("/articles/[^/]+/comments.*")) {
      return DOMAIN_COMMENTS;
    }

    if (path.startsWith("/articles") || path.startsWith("/tags")) {
      return DOMAIN_ARTICLES;
    }

    return null;
  }
}
