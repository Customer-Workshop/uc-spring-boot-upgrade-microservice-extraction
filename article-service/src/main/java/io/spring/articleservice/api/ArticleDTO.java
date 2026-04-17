package io.spring.articleservice.api;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTO {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private String userId;
  private List<String> tagList;
  private Instant createdAt;
  private Instant updatedAt;
}
