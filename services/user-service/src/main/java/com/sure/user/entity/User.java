package com.sure.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password_digest", nullable = false)
    private String passwordDigest;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "member";

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "onboarded_at")
    private LocalDateTime onboardedAt;

    @Column(name = "otp_secret")
    private String otpSecret;

    @Column(name = "otp_required")
    @Builder.Default
    private boolean otpRequired = false;

    @Column(name = "ai_enabled")
    @Builder.Default
    private boolean aiEnabled = false;

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
