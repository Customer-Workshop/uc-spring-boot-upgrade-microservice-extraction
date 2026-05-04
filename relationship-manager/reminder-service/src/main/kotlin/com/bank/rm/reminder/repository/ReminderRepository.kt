package com.bank.rm.reminder.repository

import com.bank.rm.reminder.domain.Reminder
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface ReminderRepository : JpaRepository<Reminder, UUID> {
    fun findByStatusAndScheduledAtBefore(status: String, before: LocalDateTime): List<Reminder>
    fun findByCustomerIdOrderByScheduledAtDesc(customerId: UUID): List<Reminder>
}
