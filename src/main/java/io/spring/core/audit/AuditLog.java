package io.spring.core.audit;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
public class AuditLog {
  private String id;
  private String userId;
  private String action;
  private String entityType;
  private String entityId;
  private String details;
  private DateTime createdAt;

  public AuditLog(
      String userId, String action, String entityType, String entityId, String details) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.details = details;
    this.createdAt = new DateTime();
  }
}
