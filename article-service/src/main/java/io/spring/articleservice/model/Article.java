package io.spring.articleservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
public class Article {

  @Id
  @Column(length = 255)
  private String id;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String body;

  @Column(name = "user_id", nullable = false, length = 255)
  private String userId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "article_tags",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  public Article(String title, String description, String body, String userId) {
    this.id = UUID.randomUUID().toString();
    this.slug = toSlug(title);
    this.title = title;
    this.description = description;
    this.body = body;
    this.userId = userId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void update(String title, String description, String body) {
    if (title != null && !title.isEmpty()) {
      this.title = title;
      this.slug = toSlug(title);
      this.updatedAt = Instant.now();
    }
    if (description != null && !description.isEmpty()) {
      this.description = description;
      this.updatedAt = Instant.now();
    }
    if (body != null && !body.isEmpty()) {
      this.body = body;
      this.updatedAt = Instant.now();
    }
  }

  public static String toSlug(String title) {
    return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\\"\\s\\?,\\.]+", "-");
  }
}
