package com.bank.rm.risk.repository

import com.bank.rm.risk.domain.PortfolioAllocation
import com.bank.rm.risk.domain.RiskAssessment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface RiskAssessmentRepository : JpaRepository<RiskAssessment, UUID> {
    fun findByCustomerIdAndIsCurrentTrue(customerId: UUID): RiskAssessment?

    @Query("SELECT r FROM RiskAssessment r WHERE r.customerId = :customerId ORDER BY r.assessedAt DESC")
    fun findByCustomerIdOrderByAssessedAtDesc(customerId: UUID): List<RiskAssessment>
}

interface PortfolioAllocationRepository : JpaRepository<PortfolioAllocation, UUID> {
    fun findByAssessmentId(assessmentId: UUID): PortfolioAllocation?
    fun findByCustomerId(customerId: UUID): List<PortfolioAllocation>
}
