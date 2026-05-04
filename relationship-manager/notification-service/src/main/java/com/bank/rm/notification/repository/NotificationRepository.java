package com.bank.rm.notification.repository;

import com.bank.rm.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
