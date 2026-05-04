package com.bank.rm.profile.repository

import com.bank.rm.profile.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByEmail(email: String): Customer?
    fun findByPhone(phone: String): Customer?
    fun findByAnonymousSessionId(sessionId: UUID): Customer?
}

interface FinancialProfileRepository : JpaRepository<FinancialProfile, UUID> {
    fun findByCustomerId(customerId: UUID): FinancialProfile?
}

interface RiskProfileRepository : JpaRepository<RiskProfile, UUID> {
    fun findByCustomerIdAndIsCurrentTrue(customerId: UUID): RiskProfile?

    @Query("SELECT r FROM RiskProfile r WHERE r.customerId = :customerId ORDER BY r.assessedAt DESC")
    fun findAllByCustomerIdOrderByAssessedAtDesc(customerId: UUID): List<RiskProfile>
}

interface CommunicationPreferenceRepository : JpaRepository<CommunicationPreference, UUID> {
    fun findByCustomerId(customerId: UUID): CommunicationPreference?
}
