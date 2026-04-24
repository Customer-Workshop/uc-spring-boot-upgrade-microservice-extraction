package com.sure.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(nullable = false)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icon;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String classification = "expense";

    @Column(name = "lucide_icon", length = 50)
    private String lucideIcon;

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
