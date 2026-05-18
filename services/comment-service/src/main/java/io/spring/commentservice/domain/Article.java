package io.spring.commentservice.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Article {
  private String id;
  private String userId;
  private String slug;
  private String title;
  private String description;
  private String body;
  private DateTime createdAt;
  private DateTime updatedAt;
}
