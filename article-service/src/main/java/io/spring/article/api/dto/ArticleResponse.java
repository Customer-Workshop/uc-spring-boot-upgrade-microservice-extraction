package io.spring.article.api.dto;

import io.spring.article.domain.Article;
import io.spring.article.domain.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private List<String> tagList;
  private String userId;
  private DateTime createdAt;
  private DateTime updatedAt;

  public static ArticleResponse from(Article article) {
    return ArticleResponse.builder()
        .id(article.getId())
        .slug(article.getSlug())
        .title(article.getTitle())
        .description(article.getDescription())
        .body(article.getBody())
        .tagList(
            article.getTags() != null
                ? article.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of())
        .userId(article.getUserId())
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .build();
  }
}
