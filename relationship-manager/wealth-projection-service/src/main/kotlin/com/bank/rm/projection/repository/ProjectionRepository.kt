package com.bank.rm.projection.repository

import com.bank.rm.projection.domain.WealthProjection
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WealthProjectionRepository : JpaRepository<WealthProjection, UUID> {
    fun findFirstByCustomerIdOrderByCreatedAtDesc(customerId: UUID): WealthProjection?
    fun findAllByCustomerId(customerId: UUID): List<WealthProjection>
}
