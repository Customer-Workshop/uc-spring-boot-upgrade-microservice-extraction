package com.sure.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id")
    private Security security;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 19, scale = 6)
    @Builder.Default
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "cost_basis", precision = 19, scale = 4)
    private BigDecimal costBasis;

    @Column(name = "cost_basis_source", length = 20)
    private String costBasisSource;

    @Column(name = "cost_basis_locked")
    @Builder.Default
    private boolean costBasisLocked = false;

    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
