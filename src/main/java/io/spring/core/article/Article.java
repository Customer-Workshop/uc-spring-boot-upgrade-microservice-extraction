package io.spring.core.article;

import static java.util.stream.Collectors.toList;

import io.spring.Util;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * Domain entity representing a blog article.
 *
 * <p>Articles are identified by a UUID and addressed externally via a URL-friendly slug derived
 * from the title. Each article belongs to a single author and may have zero or more tags.
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Article {
  private String userId;
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private List<Tag> tags;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Article(
      String title, String description, String body, List<String> tagList, String userId) {
    this(title, description, body, tagList, userId, new DateTime());
  }

  public Article(
      String title,
      String description,
      String body,
      List<String> tagList,
      String userId,
      DateTime createdAt) {
    this.id = UUID.randomUUID().toString();
    this.slug = toSlug(title);
    this.title = title;
    this.description = description;
    this.body = body;
    this.tags = new HashSet<>(tagList).stream().map(Tag::new).collect(toList());
    this.userId = userId;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  /**
   * Updates the article's mutable fields. Only non-empty values are applied; the slug is
   * regenerated when the title changes.
   *
   * @param title the new title, or empty/null to keep the current title
   * @param description the new description, or empty/null to keep the current description
   * @param body the new body, or empty/null to keep the current body
   */
  public void update(String title, String description, String body) {
    if (!Util.isEmpty(title)) {
      this.title = title;
      this.slug = toSlug(title);
      this.updatedAt = new DateTime();
    }
    if (!Util.isEmpty(description)) {
      this.description = description;
      this.updatedAt = new DateTime();
    }
    if (!Util.isEmpty(body)) {
      this.body = body;
      this.updatedAt = new DateTime();
    }
  }

  /**
   * Converts a title to a URL-friendly slug by lowercasing and replacing special characters with
   * hyphens.
   *
   * @param title the article title
   * @return the slug string
   */
  public static String toSlug(String title) {
    return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\’|\\”|\\s\\?\\,\\.]+", "-");
  }
}
