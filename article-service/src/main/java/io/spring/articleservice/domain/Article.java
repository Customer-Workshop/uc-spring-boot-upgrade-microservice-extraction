package io.spring.articleservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor
public class Article {

  @Id private String id;

  @Column(name = "user_id")
  @Setter
  private String userId;

  @Setter private String slug;
  @Setter private String title;
  @Setter private String description;
  @Setter private String body;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  @Setter
  private Instant updatedAt;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
  @JoinTable(
      name = "article_tags",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags = new ArrayList<>();

  public Article(String title, String description, String body, String userId) {
    this.id = UUID.randomUUID().toString();
    this.slug = toSlug(title);
    this.title = title;
    this.description = description;
    this.body = body;
    this.userId = userId;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
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
    return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\\"\\s\\?\\,\\.]+", "-");
  }
}
