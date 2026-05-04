CREATE TABLE notifications (
    id                UUID PRIMARY KEY,
    customer_id       UUID NOT NULL,
    type              VARCHAR(30) NOT NULL,
    channel           VARCHAR(20) NOT NULL,
    subject           VARCHAR(500) NOT NULL,
    body              TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    recipient_address VARCHAR(200) NOT NULL,
    external_id       VARCHAR(200),
    sent_at           TIMESTAMPTZ,
    delivered_at      TIMESTAMPTZ,
    failure_reason    TEXT,
    retry_count       INT DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_customer ON notifications(customer_id);
CREATE INDEX idx_notif_status ON notifications(status);
