CREATE TABLE reminders (
    id UUID PRIMARY KEY, customer_id UUID NOT NULL, follow_up_instance_id UUID,
    channel VARCHAR(20) NOT NULL, scheduled_at TIMESTAMP NOT NULL, message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', sent_at TIMESTAMPTZ,
    retry_count INT DEFAULT 0, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_rem_status ON reminders(status, scheduled_at);
