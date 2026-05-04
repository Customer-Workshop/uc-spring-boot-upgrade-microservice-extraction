package com.bank.rm.product.domain;

import com.bank.rm.common.dto.ProductCategory;
import com.bank.rm.common.dto.ProductRiskLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private ProductRiskLevel riskLevel;

    @Column(name = "expected_return_min", precision = 5, scale = 2)
    private BigDecimal expectedReturnMin;

    @Column(name = "expected_return_max", precision = 5, scale = 2)
    private BigDecimal expectedReturnMax;

    @Column(name = "min_investment", precision = 15, scale = 2)
    private BigDecimal minInvestment;

    @Column(name = "max_investment", precision = 15, scale = 2)
    private BigDecimal maxInvestment;

    @Column(name = "tax_benefit_section")
    private String taxBenefitSection;

    @Column(columnDefinition = "jsonb")
    private String features;

    @Column(name = "is_active")
    private boolean active;

    private int priority;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Product() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductCategory getCategory() { return category; }
    public ProductRiskLevel getRiskLevel() { return riskLevel; }
    public BigDecimal getExpectedReturnMin() { return expectedReturnMin; }
    public BigDecimal getExpectedReturnMax() { return expectedReturnMax; }
    public BigDecimal getMinInvestment() { return minInvestment; }
    public String getTaxBenefitSection() { return taxBenefitSection; }
    public boolean isActive() { return active; }
}
