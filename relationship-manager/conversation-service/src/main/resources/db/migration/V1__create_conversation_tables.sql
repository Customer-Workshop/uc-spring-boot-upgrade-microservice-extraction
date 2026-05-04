CREATE TABLE conversations (
    id                   UUID PRIMARY KEY,
    customer_id          UUID,
    anonymous_session_id UUID,
    current_phase        VARCHAR(30) NOT NULL DEFAULT 'GREETING',
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    channel              VARCHAR(20) NOT NULL DEFAULT 'WEB',
    extracted_data       JSONB NOT NULL DEFAULT '{}',
    llm_context          JSONB NOT NULL DEFAULT '{}',
    assigned_rm_id       UUID,
    summary              TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conv_customer ON conversations(customer_id);
CREATE INDEX idx_conv_session ON conversations(anonymous_session_id);
CREATE INDEX idx_conv_status ON conversations(status);

CREATE TABLE messages (
    id                  UUID PRIMARY KEY,
    conversation_id     UUID NOT NULL REFERENCES conversations(id),
    sender_type         VARCHAR(20) NOT NULL,
    sender_id           UUID,
    content_type        VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content             TEXT NOT NULL,
    metadata            JSONB DEFAULT '{}',
    phase               VARCHAR(30),
    entities_extracted  JSONB DEFAULT '{}',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_msg_conversation ON messages(conversation_id);
CREATE INDEX idx_msg_created ON messages(conversation_id, created_at);

CREATE TABLE conversation_templates (
    id               UUID PRIMARY KEY,
    phase            VARCHAR(30) NOT NULL,
    system_prompt    TEXT NOT NULL,
    required_entities JSONB DEFAULT '[]',
    validation_rules JSONB DEFAULT '{}',
    next_phase       VARCHAR(30),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    version          INT NOT NULL DEFAULT 1
);
