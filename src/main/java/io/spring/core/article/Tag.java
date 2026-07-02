package io.spring.core.article;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing an article tag.
 *
 * <p>Tags are unique by name and identified internally by a UUID.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "name")
public class Tag {
  private String id;
  private String name;

  public Tag(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }
}
