package com.bank.rm.followup.repository

import com.bank.rm.common.dto.FollowUpStatus
import com.bank.rm.followup.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface FollowUpScheduleRepository : JpaRepository<FollowUpSchedule, UUID> {
    fun findByCustomerIdAndIsActiveTrue(customerId: UUID): FollowUpSchedule?
    fun findAllByIsActiveTrue(): List<FollowUpSchedule>
}

interface FollowUpInstanceRepository : JpaRepository<FollowUpInstance, UUID> {
    fun findByCustomerIdAndStatusOrderByScheduledAtAsc(customerId: UUID, status: FollowUpStatus): List<FollowUpInstance>

    @Query("SELECT i FROM FollowUpInstance i WHERE i.scheduledAt <= :now AND i.status = 'SCHEDULED'")
    fun findDueInstances(now: LocalDateTime): List<FollowUpInstance>
}

interface ActionItemRepository : JpaRepository<ActionItem, UUID> {
    fun findByCustomerIdAndStatus(customerId: UUID, status: com.bank.rm.common.dto.ActionItemStatus): List<ActionItem>
}
