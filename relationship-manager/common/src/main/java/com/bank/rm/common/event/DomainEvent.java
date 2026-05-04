package com.bank.rm.common.event;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {
    private final UUID eventId;
    private final Instant timestamp;
    private final String source;

    protected DomainEvent(String source) {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.source = source;
    }

    public UUID getEventId() { return eventId; }
    public Instant getTimestamp() { return timestamp; }
    public String getSource() { return source; }
}
