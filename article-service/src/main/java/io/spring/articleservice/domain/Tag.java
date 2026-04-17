package io.spring.articleservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor
public class Tag {

  @Id private String id;

  @Column(nullable = false)
  private String name;

  public Tag(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }
}
