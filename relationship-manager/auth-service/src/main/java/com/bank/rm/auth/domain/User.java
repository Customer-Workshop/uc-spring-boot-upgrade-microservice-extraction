package com.bank.rm.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String name;
    private String phone;

    @Column(name = "anonymous_session_id")
    private UUID anonymousSessionId;

    @Column(name = "is_anonymous")
    private boolean anonymous;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected User() {}

    public User(UUID id, String email, String passwordHash, String name, String phone,
                UUID anonymousSessionId, boolean anonymous) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.anonymousSessionId = anonymousSessionId;
        this.anonymous = anonymous;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public UUID getAnonymousSessionId() { return anonymousSessionId; }
    public boolean isAnonymous() { return anonymous; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setAnonymousSessionId(UUID anonymousSessionId) { this.anonymousSessionId = anonymousSessionId; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
