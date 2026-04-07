package io.spring.core.project;

import io.spring.Util;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Project {
  private String id;
  private String name;
  private String description;
  private String client;
  private DateTime startDate;
  private String status;
  private String userId;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Project(
      String name, String description, String client, DateTime startDate, String status,
      String userId) {
    this(name, description, client, startDate, status, userId, new DateTime());
  }

  public Project(
      String name,
      String description,
      String client,
      DateTime startDate,
      String status,
      String userId,
      DateTime createdAt) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.description = description;
    this.client = client;
    this.startDate = startDate;
    this.status = status;
    this.userId = userId;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public void update(String name, String description, String client, DateTime startDate,
      String status) {
    if (!Util.isEmpty(name)) {
      this.name = name;
      this.updatedAt = new DateTime();
    }
    if (!Util.isEmpty(description)) {
      this.description = description;
      this.updatedAt = new DateTime();
    }
    if (!Util.isEmpty(client)) {
      this.client = client;
      this.updatedAt = new DateTime();
    }
    if (startDate != null) {
      this.startDate = startDate;
      this.updatedAt = new DateTime();
    }
    if (!Util.isEmpty(status)) {
      this.status = status;
      this.updatedAt = new DateTime();
    }
  }
}
