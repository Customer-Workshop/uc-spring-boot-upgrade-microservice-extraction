package com.bank.rm.profile.domain;

import com.bank.rm.common.dto.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    private String name;
    private String phone;
    private String email;
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_source")
    private IncomeSource incomeSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_range")
    private IncomeRange incomeRange;

    @Column(name = "current_savings", precision = 15, scale = 2)
    private BigDecimal currentSavings;

    @Column(name = "current_investments", precision = 15, scale = 2)
    private BigDecimal currentInvestments;

    @Column(name = "monthly_expenses", precision = 15, scale = 2)
    private BigDecimal monthlyExpenses;

    @Column(name = "retirement_target", precision = 15, scale = 2)
    private BigDecimal retirementTarget;

    @Column(name = "retirement_age")
    private Integer retirementAge;

    private Integer dependents;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category")
    private RiskCategory riskCategory;

    @Column(name = "risk_score")
    private Double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel")
    private CommunicationChannel preferredChannel;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected CustomerProfile() {}

    public CustomerProfile(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public AgeGroup getAgeGroup() { return ageGroup; }
    public void setAgeGroup(AgeGroup ageGroup) { this.ageGroup = ageGroup; }
    public IncomeSource getIncomeSource() { return incomeSource; }
    public void setIncomeSource(IncomeSource incomeSource) { this.incomeSource = incomeSource; }
    public IncomeRange getIncomeRange() { return incomeRange; }
    public void setIncomeRange(IncomeRange incomeRange) { this.incomeRange = incomeRange; }
    public BigDecimal getCurrentSavings() { return currentSavings; }
    public void setCurrentSavings(BigDecimal currentSavings) { this.currentSavings = currentSavings; }
    public BigDecimal getCurrentInvestments() { return currentInvestments; }
    public void setCurrentInvestments(BigDecimal currentInvestments) { this.currentInvestments = currentInvestments; }
    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
    public BigDecimal getRetirementTarget() { return retirementTarget; }
    public void setRetirementTarget(BigDecimal retirementTarget) { this.retirementTarget = retirementTarget; }
    public Integer getRetirementAge() { return retirementAge; }
    public void setRetirementAge(Integer retirementAge) { this.retirementAge = retirementAge; }
    public Integer getDependents() { return dependents; }
    public void setDependents(Integer dependents) { this.dependents = dependents; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public void setRiskCategory(RiskCategory riskCategory) { this.riskCategory = riskCategory; }
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public CommunicationChannel getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(CommunicationChannel preferredChannel) { this.preferredChannel = preferredChannel; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
