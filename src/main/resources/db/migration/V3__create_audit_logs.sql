create table audit_logs (
  id varchar(255) primary key,
  user_id varchar(255),
  action varchar(50) not null,
  entity_type varchar(100) not null,
  entity_id varchar(255),
  details text,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create index idx_audit_logs_user_id on audit_logs (user_id);
create index idx_audit_logs_entity_type on audit_logs (entity_type);
create index idx_audit_logs_created_at on audit_logs (created_at);
