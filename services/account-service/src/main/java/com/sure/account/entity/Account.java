package com.sure.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "family_id", nullable = false, columnDefinition = "CHAR(36)")
    private String familyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    private String subtype;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "institution_url")
    private String institutionUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "scheduled_for_deletion")
    @Builder.Default
    private boolean scheduledForDeletion = false;

    @Column(name = "hide_from_enrich")
    @Builder.Default
    private boolean hideFromEnrich = false;

    @Column(name = "excluded_from_totals")
    @Builder.Default
    private boolean excludedFromTotals = false;

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
