package com.bank.rm.notification.repository

import com.bank.rm.common.dto.NotificationStatus
import com.bank.rm.notification.domain.Notification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: UUID): List<Notification>
    fun findByStatus(status: NotificationStatus): List<Notification>
}
