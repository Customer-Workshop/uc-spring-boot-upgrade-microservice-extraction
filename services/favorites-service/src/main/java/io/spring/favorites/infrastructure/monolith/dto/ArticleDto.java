package io.spring.favorites.infrastructure.monolith.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleDto {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private DateTime createdAt;
  private DateTime updatedAt;
  private List<String> tagList;
  private ProfileDto author;
}
