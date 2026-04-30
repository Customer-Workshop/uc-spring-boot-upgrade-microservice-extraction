package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.audit.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {
  void insert(@Param("auditLog") AuditLog auditLog);
}
