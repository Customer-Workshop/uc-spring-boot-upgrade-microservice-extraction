package com.sure.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entry {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "entryable_type", nullable = false)
    private String entryableType;

    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean excluded = false;

    @Column(name = "enriched_at")
    private LocalDateTime enrichedAt;

    @Column(name = "marked_as_transfer")
    @Builder.Default
    private boolean markedAsTransfer = false;

    @Column(name = "plaid_id")
    private String plaidId;

    @Builder.Default
    private boolean pending = false;

    @Column(name = "parent_entry_id", columnDefinition = "CHAR(36)")
    private String parentEntryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
