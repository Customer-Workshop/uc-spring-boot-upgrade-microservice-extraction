CREATE TABLE followup_schedules (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL,
    frequency       VARCHAR(20) NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    next_follow_up_at TIMESTAMP NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    total_completed INT DEFAULT 0,
    total_missed    INT DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fs_customer ON followup_schedules(customer_id);
CREATE INDEX idx_fs_active ON followup_schedules(is_active);

CREATE TABLE followup_instances (
    id                UUID PRIMARY KEY,
    schedule_id       UUID NOT NULL REFERENCES followup_schedules(id),
    customer_id       UUID NOT NULL,
    scheduled_at      TIMESTAMP NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    agenda            JSONB DEFAULT '{}',
    previous_summary  JSONB,
    conversation_id   UUID,
    completed_at      TIMESTAMPTZ,
    reminder_sent_at  TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fi_schedule ON followup_instances(schedule_id);
CREATE INDEX idx_fi_customer ON followup_instances(customer_id);
CREATE INDEX idx_fi_status ON followup_instances(status, scheduled_at);

CREATE TABLE action_items (
    id                  UUID PRIMARY KEY,
    customer_id         UUID NOT NULL,
    follow_up_instance_id UUID,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    assigned_to         VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    due_date            TIMESTAMP,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_customer ON action_items(customer_id);
CREATE INDEX idx_ai_status ON action_items(status);
