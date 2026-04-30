package io.spring.core.audit;

import io.spring.infrastructure.mybatis.mapper.AuditLogMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuditLogService {
  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
  private AuditLogMapper auditLogMapper;

  public void log(
      String userId, String action, String entityType, String entityId, String details) {
    try {
      AuditLog auditLog = new AuditLog(userId, action, entityType, entityId, details);
      auditLogMapper.insert(auditLog);
    } catch (Exception e) {
      log.error(
          "Failed to write audit log: action={}, entityType={}, entityId={}",
          action,
          entityType,
          entityId,
          e);
    }
  }
}
