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
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "family_id", nullable = false, columnDefinition = "CHAR(36)")
    private String familyId;

    @Column(nullable = false)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icon;

    @Column(name = "parent_id", columnDefinition = "CHAR(36)")
    private String parentId;

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
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
