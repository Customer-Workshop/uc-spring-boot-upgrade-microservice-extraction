package com.bank.rm.reminder.repository;

import com.bank.rm.reminder.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.scheduledAt <= :cutoff")
    List<Reminder> findPendingDue(LocalDateTime cutoff);
}
