package io.spring.api.internal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalArticleDto {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private DateTime createdAt;
  private DateTime updatedAt;
  private List<String> tagList;
  private InternalProfileDto author;
}
