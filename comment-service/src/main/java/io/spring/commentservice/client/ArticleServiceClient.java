package io.spring.commentservice.client;

import java.util.Optional;

public interface ArticleServiceClient {

  Optional<ArticleResponse> getArticleBySlug(String slug);

  class ArticleResponse {
    private String id;
    private String slug;
    private String userId;

    public ArticleResponse() {}

    public ArticleResponse(String id, String slug, String userId) {
      this.id = id;
      this.slug = slug;
      this.userId = userId;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getSlug() {
      return slug;
    }

    public void setSlug(String slug) {
      this.slug = slug;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }
}
