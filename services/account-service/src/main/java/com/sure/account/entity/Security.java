package com.sure.account.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "securities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Security {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 20)
    private String ticker;

    private String name;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "exchange_mic", length = 10)
    private String exchangeMic;

    @Column(name = "exchange_acronym", length = 20)
    private String exchangeAcronym;

    @Column(name = "logo_url")
    private String logoUrl;

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
